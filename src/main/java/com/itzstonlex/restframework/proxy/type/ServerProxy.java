package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.context.RestBody;
import com.itzstonlex.restframework.api.RestOption;
import com.itzstonlex.restframework.api.RestParam;
import com.itzstonlex.restframework.api.RestServer;
import com.itzstonlex.restframework.api.authentication.BasicServletAuthenticator;
import com.itzstonlex.restframework.api.authentication.RestAuthResult;
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.authentication.RestAuthenticationResult;
import com.itzstonlex.restframework.api.context.request.RestRequestSignature;
import com.itzstonlex.restframework.api.context.request.RestRequestContext;
import com.itzstonlex.restframework.api.context.response.RestResponse;
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
import java.util.*;
import java.util.concurrent.TimeoutException;

@FieldDefaults(makeFinal = true)
public class ServerProxy implements MethodHandler {

    /**
     * Creating Basic Java-Servlets Authenticator implement.
     *
     * @param serverSuperclass - REST Server-Service type.
     * @return - Basic Authenticator.
     */
    private static Authenticator createAuthenticator(Class<?> serverSuperclass) {
        return new BasicServletAuthenticator(
                serverSuperclass.getDeclaredAnnotation(RestAuthentication.class), "RestServerRealm"
        );
    }

    /**
     * Wrapping the server REST service in Proxy
     * and initializing the finished instance.
     *
     * @param serverSuperclass - REST Server-Service type.
     * @param initargsTypes - Server type constructor initial arguments types.
     * @param initargs - Server type constructor initial arguments.
     *
     * @return - Proxied server instance.
     */
    @SneakyThrows
    public static <T> T wrap(Class<?> serverSuperclass, Class<?>[] initargsTypes, Object[] initargs) {
        ProxyFactory proxyFactory = new ProxyFactory();

        proxyFactory.setUseCache(true);
        proxyFactory.setUseWriteReplace(true);
        proxyFactory.setSuperclass(serverSuperclass);

        ServerProxy serverProxy = new ServerProxy(serverSuperclass);
        @SuppressWarnings("unchecked") T proxyInstance = (T) proxyFactory.create(initargsTypes, initargs, serverProxy);

        serverProxy.setProxyInstance(proxyInstance);

        return proxyInstance;
    }

    private Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap = new HashMap<>();

    @NonFinal
    private Authenticator authenticator;

    @NonFinal
    private Set<Method> authenticationHandlersSet;

    @Setter(AccessLevel.PRIVATE)
    @NonFinal
    private Object proxyInstance;

    private ServerProxy(Class<?> serverSuperclass) {
        RestServer restServer = RestUtilities.getServerAnnotation(serverSuperclass);
        RestOption[] restFlagsArray = RestUtilities.getFlagsAnnotations(serverSuperclass);

        if (restServer == null) {
            throw new RuntimeException("Annotation @RestServer not found for " + serverSuperclass);
        }

        try {
            HttpServer httpServer = HttpServer.create();

            if (serverSuperclass.isAnnotationPresent(RestAuthentication.class)) {

                authenticator = createAuthenticator(serverSuperclass);
                authenticationHandlersSet = new HashSet<>();
            }

            for (Method method : serverSuperclass.getDeclaredMethods()) {
                if (RestUtilities.checkAndSaveExceptionHandler(method, exceptionHandlersMap)) {
                    continue;
                }

                if (authenticator != null && method.isAnnotationPresent(RestAuthenticationResult.class)) {
                    authenticationHandlersSet.add(method);
                    continue;
                }

                if (!method.getReturnType().isAssignableFrom(RestResponse.class)) {
                    continue;
                }

                RestRequestSignature signature = RestUtilities.createRequestSignature(method);
                String uri = restServer.defaultContext() + signature.getUri();

                HttpContext context = httpServer.createContext(uri);
                context.setHandler(new ExchangedMethod(RestUtilities.getOptionsTypes(restFlagsArray), signature, uri, method));
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

        private RestOption.Type[] options;

        private RestRequestSignature signature;
        private String uri;

        private Method declaredMethod;

        /**
         * Sending a response by HTTP.
         *
         * @param exchange - Java-Servlets incoming exchange.
         * @param statusCode - Response HTTP code.
         * @param responseBytes - Response HTTP body as bytes array.
         */
        private void sendResponse(HttpExchange exchange, int statusCode, byte[] responseBytes)
        throws IOException {

            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {

                responseBody.write(responseBytes);
                responseBody.flush();
            }
        }

        /**
         * Sending a response by HTTP by executing a
         * declared proxy method.
         *
         * @param exchange - Java-Servlets incoming exchange.
         * @param invokeArgs - Declared proxy method signature arguments.
         */
        private void sendResponse(HttpExchange exchange, Object[] invokeArgs)
        throws Exception {

            RestResponse response = (RestResponse) declaredMethod.invoke(proxyInstance, invokeArgs);
            byte[] responseBytes = response.getBody().getAsByteArray();

            sendResponse(exchange, response.getStatusCode(), responseBytes);
        }

        /**
         * Reading a request body input as String.
         *
         * @param exchange - Java-Servlets incoming exchange.
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        private String readRequestBody(HttpExchange exchange)
        throws Exception {

            try (InputStream requestBody = exchange.getRequestBody()) {

                byte[] requestBytes = new byte[requestBody.available()];
                requestBody.read(requestBytes);

                return new String(requestBytes, 0, requestBytes.length);
            }
        }

        /**
         * Creating proxy method signature arguments list
         * from HTTP exchange request data.
         *
         * @param uri - Request URI.
         * @param exchange - Java-Servlets incoming exchange.
         */
        private List<Object> createMethodArgumentsList(String uri, HttpExchange exchange)
        throws Exception {

            String requestBodyMessage = readRequestBody(exchange);
            String linkParameters = uri.contains("?") ? uri.substring(uri.indexOf("?") + 1) : null;

            List<Object> methodArgumentsList = new ArrayList<>();

            Parameter[] parametersArray = declaredMethod.getParameters();

            for (Parameter parameter : parametersArray) {

                if (parameter.isAnnotationPresent(RestParam.class)) {

                    if (parameter.getType().isAssignableFrom(RestRequestContext.class)) {
                        RestRequestContext requestContext = RestRequestContext.create(exchange.getRequestMethod(), uri);

                        requestContext.setBody(RestBody.fromString(requestBodyMessage));

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

        /**
         * Handling of HTTP exchange data.
         *
         * @param exchange - The exchange containing the request from the
         *      client and used to send the response
         */
        @Override
        public void handle(HttpExchange exchange)
        throws IOException {

            try {
                String requestContext = exchange.getRequestURI().toString();

                if (authenticator != null) {
                    Authenticator.Result result = authenticator.authenticate(exchange);

                    boolean isSuccess = result instanceof Authenticator.Success;

                    for (Method authResultHandler : authenticationHandlersSet) {
                        authResultHandler.invoke(proxyInstance, RestAuthResult.parse(isSuccess));
                    }

                    if (!isSuccess) {
                        sendResponse(exchange, 403, "Wrong authentication credentials".getBytes());
                        return;
                    }
                }

                if (!requestContext.split("\\?")[0].endsWith(uri) || !signature.getMethod().equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendResponse(exchange, 404, uri.getBytes());
                    return;
                }
                long startTime = System.currentTimeMillis();

                Object[] invokeArgs = createMethodArgumentsList(requestContext, exchange).toArray();

                if (System.currentTimeMillis() - startTime > signature.getTimeout()) {
                    throw new TimeoutException(declaredMethod.toString());
                }

                sendResponse(exchange, invokeArgs);
            }
            catch (Throwable exception) {
                sendResponse(exchange, 500, exception.getMessage().getBytes());
                Throwable lastCause = RestUtilities.getLastCause(exception);

                if (!RestUtilities.handleException(proxyInstance, lastCause, exceptionHandlersMap)) {

                    if (RestUtilities.hasFlag(options, RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)) {
                        lastCause.printStackTrace();
                    }
                }
            }
        }
    }

}
