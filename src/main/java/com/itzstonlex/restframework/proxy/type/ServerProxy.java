package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.RestBody;
import com.itzstonlex.restframework.api.RestFlag;
import com.itzstonlex.restframework.api.RestParam;
import com.itzstonlex.restframework.api.RestServer;
import com.itzstonlex.restframework.api.authentication.BasicServletAuthenticator;
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.request.RestRequest;
import com.itzstonlex.restframework.api.request.RestRequestContext;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.util.RestUtilities;
import com.sun.net.httpserver.*;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@FieldDefaults(makeFinal = true)
public class ServerProxy implements MethodHandler {

    private static Authenticator createAuthenticator(Class<?> serverSuperclass) {
        return new BasicServletAuthenticator(
                serverSuperclass.getDeclaredAnnotation(RestAuthentication.class), "RestServerRealm"
        );
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T wrap(Class<?> serverSuperclass, Class<?>[] initargsTypes, Object[] initargs) {
        ProxyFactory proxyFactory = new ProxyFactory();

        proxyFactory.setUseCache(true);
        proxyFactory.setUseWriteReplace(true);
        proxyFactory.setSuperclass(serverSuperclass);

        ServerProxy serverProxy = new ServerProxy(serverSuperclass);
        T proxyInstance = (T) proxyFactory.create(initargsTypes, initargs, serverProxy);

        serverProxy.setProxyInstance(proxyInstance);

        return proxyInstance;
    }

    private Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap = new HashMap<>();

    @Setter(AccessLevel.PRIVATE)
    @NonFinal
    private Object proxyInstance;

    private ServerProxy(Class<?> serverSuperclass) {
        RestServer restServer = RestUtilities.getServerAnnotation(serverSuperclass);
        RestFlag[] restFlagsArray = RestUtilities.getFlagsAnnotations(serverSuperclass);

        if (restServer == null) {
            throw new RuntimeException("Annotation @RestServer not found for " + serverSuperclass);
        }

        try {
            HttpServer httpServer = HttpServer.create();

            for (Method method : serverSuperclass.getDeclaredMethods()) {
                if (RestUtilities.checkAndSaveExceptionHandler(method, exceptionHandlersMap)) {
                    continue;
                }

                if (!method.getReturnType().isAssignableFrom(RestResponse.class)) {
                    continue;
                }

                RestRequest request = RestUtilities.newRestRequest(method);
                String contextName = restServer.defaultContext() + request.getContext();

                HttpContext context = httpServer.createContext(contextName);
                context.setHandler(new ExchangedMethod(RestUtilities.getRestFlagsTypes(restFlagsArray), request, contextName, method));

                if (serverSuperclass.isAnnotationPresent(RestAuthentication.class)) {
                    context.setAuthenticator(createAuthenticator(serverSuperclass));
                }
            }

            httpServer.bind(new InetSocketAddress(restServer.host(), restServer.port()), 10);
            httpServer.start();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return proceed.invoke(self, args);
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private class ExchangedMethod implements HttpHandler {

        private RestFlag.Type[] restFlagsTypes;

        private RestRequest request;
        private String restContext;

        private Method declaredMethod;

        private void sendResponse(HttpExchange exchange, int statusCode, byte[] responseBytes)
        throws IOException {

            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {

                responseBody.write(responseBytes);
                responseBody.flush();
            }
        }

        private void sendResponse(HttpExchange exchange, Object[] invokeArgs)
        throws Exception {

            RestResponse response = (RestResponse) declaredMethod.invoke(proxyInstance, invokeArgs);
            byte[] responseBytes = response.getBody().getAsByteArray();

            sendResponse(exchange, response.getStatusCode(), responseBytes);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private String readRequestBody(HttpExchange exchange)
        throws Exception {

            try (InputStream requestBody = exchange.getRequestBody()) {

                byte[] requestBytes = new byte[requestBody.available()];
                requestBody.read(requestBytes);

                return new String(requestBytes, 0, requestBytes.length);
            }
        }

        private List<Object> createMethodArgumentsList(String requestContextPath, HttpExchange exchange)
        throws Exception {

            String requestBodyMessage = readRequestBody(exchange);
            String linkParameters = requestContextPath.contains("?") ? requestContextPath.substring(requestContextPath.indexOf("?") + 1) : null;

            List<Object> methodArgumentsList = new ArrayList<>();

            Parameter[] parametersArray = declaredMethod.getParameters();

            for (Parameter parameter : parametersArray) {

                if (parameter.isAnnotationPresent(RestParam.class)) {

                    if (parameter.getType().isAssignableFrom(RestRequestContext.class)) {
                        RestRequestContext requestContext = RestRequestContext.create(exchange.getRequestMethod(), requestContextPath);

                        requestContext.setBody(RestBody.asText(requestBodyMessage));

                        requestContext.getHeaders().putAll(exchange.getRequestHeaders());
                        methodArgumentsList.add(requestContext);

                        continue;
                    }

                    if (linkParameters == null) {
                        throw new NullPointerException("URL Parameters is not found!");
                    }

                    String name = parameter.getDeclaredAnnotation(RestParam.class).value();
                    String linkParameterPrefix = (name + "=");

                    int prefixIndexBegin = linkParameters.indexOf(linkParameterPrefix) + linkParameterPrefix.length();
                    int prefixIndexEnd = linkParameters.indexOf("&", prefixIndexBegin);

                    if (!linkParameters.contains(linkParameterPrefix)) {
                        throw new NullPointerException("Parameter " + parameter + " (" + name + ") is not found in URL: " + exchange.getRequestURI().toURL());
                    }

                    Object value = RestUtilities.GSON.fromJson(linkParameters.substring(prefixIndexBegin, prefixIndexEnd > 0 ? prefixIndexEnd : linkParameters.length()),
                            parameter.getType());

                    methodArgumentsList.add(value);
                    continue;
                }

                throw new IllegalArgumentException(parameter.toString());
            }

            return methodArgumentsList;
        }

        @Override
        public void handle(HttpExchange exchange)
        throws IOException {

            String requestContext = exchange.getRequestURI().toString();

            Authenticator authenticator = exchange.getHttpContext().getAuthenticator();

            if (authenticator != null) {
                Authenticator.Result result = authenticator.authenticate(exchange);



                if (!(result instanceof Authenticator.Success)) {
                    sendResponse(exchange, 403, "Wrong authentication credentials".getBytes());
                }
            }

            if (!requestContext.split("\\?")[0].endsWith(restContext) || !request.getMethod().equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 404, restContext.getBytes());
                return;
            }

            try {
                long startTime = System.currentTimeMillis();

                Object[] invokeArgs = createMethodArgumentsList(requestContext, exchange).toArray();

                if (System.currentTimeMillis() - startTime > request.getTimeout()) {
                    throw new TimeoutException(declaredMethod.toString());
                }

                sendResponse(exchange, invokeArgs);
            }
            catch (Throwable exception) {
                sendResponse(exchange, 500, exception.getMessage().getBytes());
                Throwable lastCause = RestUtilities.getLastCause(exception);

                if (!RestUtilities.handleException(proxyInstance, lastCause, exceptionHandlersMap)) {

                    if (RestUtilities.hasFlag(restFlagsTypes, RestFlag.Type.THROW_UNHANDLED_EXCEPTIONS)) {
                        lastCause.printStackTrace();
                    }
                }
            }
        }
    }

}
