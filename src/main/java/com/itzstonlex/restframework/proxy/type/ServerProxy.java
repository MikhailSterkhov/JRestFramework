package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.RestExceptionHandler;
import com.itzstonlex.restframework.api.RestFlag;
import com.itzstonlex.restframework.api.RestParam;
import com.itzstonlex.restframework.api.RestServer;
import com.itzstonlex.restframework.api.method.RequestMethod;
import com.itzstonlex.restframework.api.request.RestRequest;
import com.itzstonlex.restframework.api.request.RestRequestMessage;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.util.RestUtilities;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;

@FieldDefaults(makeFinal = true)
public class ServerProxy implements MethodHandler {

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

    @SuppressWarnings("unchecked")
    private ServerProxy(Class<?> serverSuperclass) {
        RestServer restServer = RestUtilities.getServerAnnotation(serverSuperclass);
        RestFlag[] restFlagsArray = RestUtilities.getFlagsAnnotations(serverSuperclass);

        if (restServer == null) {
            throw new RuntimeException("Annotation @RestServer not found for " + serverSuperclass);
        }

        try {
            HttpServer httpServer = HttpServer.create();

            for (Method method : serverSuperclass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RestExceptionHandler.class)) {

                    if (method.getParameterCount() != 1) {
                        throw new IllegalArgumentException("Exception handler " + method + " must be have only 1 Throwable superclass in signature");
                    }

                    Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) method.getParameters()[0].getType();

                    List<Method> exceptionHandlers = exceptionHandlersMap.computeIfAbsent(exceptionType, k -> new ArrayList<>());
                    exceptionHandlers.add(method);

                    continue;
                }

                if (!method.getReturnType().isAssignableFrom(RestResponse.class)) {
                    continue;
                }

                RestRequest request = null;
                RequestMethod requestMethod = method.getDeclaredAnnotation(RequestMethod.class);

                if (requestMethod == null) {

                    Set<Class<? extends Annotation>> annotationsSet = RestUtilities.getRequestAnnotationsTypes();
                    for (Class<? extends Annotation> annotationType : annotationsSet) {

                        Annotation declaredAnnotation = method.getDeclaredAnnotation(annotationType);

                        if (declaredAnnotation != null) {
                            request = RestUtilities.newRequestByAnnotationType(declaredAnnotation);
                        }
                    }
                } else {
                    request = RestUtilities.newRequestByAnnotationType(requestMethod);
                }

                if (request == null) {
                    throw new NullPointerException(method + ": no method request");
                }

                String contextName = restServer.defaultContext() + request.getContext();

                httpServer.createContext(contextName,
                        new ExchangedMethod(RestUtilities.getRestFlagsTypes(restFlagsArray), request.getMethod(), contextName, method));
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

        // todo - apply flags.
        private RestFlag.Type[] restFlagsTypes;

        private String restMethod;
        private String restContext;

        private Method declaredMethod;

        private void sendResponse(HttpExchange exchange, Object[] invokeArgs)
        throws Exception {

            RestResponse response = (RestResponse) declaredMethod.invoke(proxyInstance, invokeArgs);
            response.setMethod(exchange.getRequestMethod());

            byte[] responseBytes = response.getBodyAsByteArray();

            exchange.sendResponseHeaders(response.getResponseCode(), responseBytes.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {

                responseBody.write(responseBytes);
                responseBody.flush();
            }
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

                if (parameter.getType().isAssignableFrom(RestRequestMessage.class)) {

                    RestRequestMessage restRequestMessage = RestRequestMessage.asText(requestBodyMessage);
                    methodArgumentsList.add(restRequestMessage);
                    continue;
                }
                else if (parameter.isAnnotationPresent(RestParam.class)) {

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
        public void handle(HttpExchange exchange) {
            String requestContext = exchange.getRequestURI().toString();

            if (!requestContext.split("\\?")[0].endsWith(restContext) || !restMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.close();
                return;
            }

            try {
                sendResponse(exchange, createMethodArgumentsList(requestContext, exchange).toArray());
            }
            catch (Exception exception) {
                exception.printStackTrace();
                // todo - handle exception by annotation.
            }
        }
    }

}