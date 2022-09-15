package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.api.RestExceptionHandler;
import com.itzstonlex.restframework.api.RestFlag;
import com.itzstonlex.restframework.api.method.RequestMethod;
import com.itzstonlex.restframework.api.request.RestRequest;
import com.itzstonlex.restframework.api.request.RestRequestMessage;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.util.RestUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpRequest;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@FieldDefaults(makeFinal = true)
public class ClientProxy implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(ClassLoader classLoader, Class<T> providerInterface) {
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{providerInterface}, new ClientProxy(providerInterface));
    }

    private Map<String, ExecutableMethod> executionsMap = new HashMap<>();
    private Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    private ClientProxy(Class<?> interfaceClass) {
        RestClient restClient = RestUtilities.getClientAnnotation(interfaceClass);
        RestFlag[] restFlagsArray = RestUtilities.getFlagsAnnotations(interfaceClass);

        if (restClient == null) {
            throw new RuntimeException("Annotation @RestClient not found for " + interfaceClass);
        }

        for (Method method : interfaceClass.getDeclaredMethods()) {

            if (method.isAnnotationPresent(RestExceptionHandler.class)) {

                if (method.getParameterCount() != 1) {
                    throw new IllegalArgumentException("Exception handler " + method + " must be have only 1 Throwable superclass in signature");
                }

                Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) method.getParameters()[0].getType();

                List<Method> exceptionHandlers = exceptionHandlersMap.computeIfAbsent(exceptionType, k -> new ArrayList<>());
                exceptionHandlers.add(method);

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
            }
            else {
                request = RestUtilities.newRequestByAnnotationType(requestMethod);
            }

            if (request == null) {
                throw new NullPointerException("no method request for " + method);
            }

            executionsMap.put(method.toString(),
                    new ExecutableMethod(restClient, RestUtilities.getRestFlagsTypes(restFlagsArray), request, method));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
    throws Exception {

        if (method.isAnnotationPresent(RestExceptionHandler.class)) {
            RestUtilities.handleException(proxy, (Throwable) args[0], exceptionHandlersMap);

            return Void.TYPE.newInstance();
        }

        ExecutableMethod executable = executionsMap.get(method.toString());

        if (executable != null) {
            return executable.execute(proxy, args).join();
        }

        throw new UnsupportedOperationException();
    }

    @Getter
    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private class ExecutableMethod {

        private RestClient restClient;
        private RestFlag.Type[] restFlagsArray;

        private RestRequest request;

        private Method method;

        public CompletableFuture<Object> execute(Object proxy, Object[] args) {
            Supplier<Object> responseSupplier = () -> {

                String fullLink = restClient.url() + request.getContext();

                if (!RestUtilities.hasFlag(restFlagsArray, RestFlag.Type.DISALLOW_SIGNATURE) && request.isUseSignature()) {
                    try {
                        fullLink += RestUtilities.makeLinkSignature(method, args);
                    }
                    catch (IOException exception) {
                        RestUtilities.handleException(proxy, exception, exceptionHandlersMap);

                        return RestResponse.create(500, "Internal Server Error", fullLink, exception.getMessage(), request.getMethod());
                    }
                }

                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    RequestBuilder requestBuilder = RequestBuilder.copy(new BasicHttpRequest(request.getMethod(), fullLink));

                    if (args != null && args.length == 1 && args[0].getClass().isAssignableFrom(RestRequestMessage.class)) {
                        RestRequestMessage message = (RestRequestMessage) args[0];

                        if (message.isNull()) {
                            throw new NullPointerException(method + " - request message is null");
                        }

                        try {
                            requestBuilder.setEntity(new StringEntity(message.getMessage()));
                        }
                        catch (UnsupportedEncodingException exception) {
                            RestUtilities.handleException(proxy, exception, exceptionHandlersMap);

                            return RestResponse.create(500, "Internal Server Error", fullLink, exception.getMessage(), request.getMethod());
                        }
                    }

                    CloseableHttpResponse apacheResponse = httpClient.execute(requestBuilder.build());
                    return makeMethodResponse(proxy, apacheResponse, fullLink, request.getMethod());
                }
                catch (IOException exception) {
                    RestUtilities.handleException(proxy, exception, exceptionHandlersMap);

                    return RestResponse.create(500, "Internal Server Error", fullLink, exception.getMessage(), request.getMethod());
                }
            };

            if (RestUtilities.hasFlag(restFlagsArray, RestFlag.Type.ASYNC_REQUESTS)) {
                return CompletableFuture.supplyAsync(responseSupplier);
            }

            return CompletableFuture.completedFuture(responseSupplier.get());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public RestResponse makeResponse(Object proxy, CloseableHttpResponse apacheResponse, String link, String method) {
            try (InputStream inputStream = apacheResponse.getEntity().getContent()) {

                byte[] arr = new byte[(int) apacheResponse.getEntity().getContentLength()];
                inputStream.read(arr);

                return RestResponse.create(

                        apacheResponse.getStatusLine().getStatusCode(),
                        apacheResponse.getStatusLine().getReasonPhrase(),

                        link, new String(arr, 0, arr.length), method
                );
            }
            catch (Exception exception) {
                RestUtilities.handleException(proxy, exception, exceptionHandlersMap);

                return RestResponse.create(500, "Internal Server Error", link, exception.getMessage(), request.getMethod());
            }
        }

        public Object makeMethodResponse(Object proxy, CloseableHttpResponse apacheResponse, String link, String method) {
            RestResponse response = makeResponse(proxy, apacheResponse, link, method);

            Class<?> returnType = this.method.getReturnType();

            if (!returnType.isAssignableFrom(RestResponse.class)) {
                return response.getBodyAsJsonObject(returnType);
            }

            return response;
        }
    }

}
