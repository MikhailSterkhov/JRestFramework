package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.request.RestRequest;
import com.itzstonlex.restframework.api.response.Responses;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.util.RestUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

    private ClientProxy(Class<?> interfaceClass) {
        RestClient restClient = RestUtilities.getClientAnnotation(interfaceClass);
        RestFlag[] restFlagsArray = RestUtilities.getFlagsAnnotations(interfaceClass);

        if (restClient == null) {
            throw new RuntimeException("Annotation @RestClient not found for " + interfaceClass);
        }

        for (Method method : interfaceClass.getDeclaredMethods()) {
            if (RestUtilities.checkAndSaveExceptionHandler(method, exceptionHandlersMap)) {
                continue;
            }

            RestRequest request = RestUtilities.newRestRequest(method);
            executionsMap.put(method.toString(),
                    new ExecutableMethod(restClient,

                            RestUtilities.getRestFlagsTypes(restFlagsArray),
                            RestUtilities.getHeaders(method),

                            request, method));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.isAnnotationPresent(RestExceptionHandler.class)) {

            ((Throwable) args[0]).printStackTrace();
            return null;
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
        private Header[] headers;

        private RestRequest request;

        private Method method;

        private RestResponse finallyException(Object proxy, Exception exception) {
            if (!RestUtilities.handleException(proxy, exception, exceptionHandlersMap)) {
                if (RestUtilities.hasFlag(restFlagsArray, RestFlag.Type.THROW_UNHANDLED_EXCEPTIONS)) {
                    exception.printStackTrace();
                }
            }

            return Responses.fromMessage(Responses.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        public CompletableFuture<Object> execute(Object proxy, Object[] args) {
            Supplier<Object> responseSupplier = () -> {

                String fullLink = restClient.url() + request.getContext();

                if (!RestUtilities.hasFlag(restFlagsArray, RestFlag.Type.DISALLOW_SIGNATURE) && request.isUseSignature()) {
                    try {
                        fullLink += RestUtilities.makeLinkSignature(method, args);
                    }
                    catch (IOException exception) {
                        return finallyException(proxy, exception);
                    }
                }

                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    RequestBuilder requestBuilder = RequestBuilder.copy(new BasicHttpRequest(request.getMethod(), fullLink));

                    for (Header header : headers) {
                        org.apache.http.Header apacheHeader = new BasicHeader(header.name(), header.value());

                        switch (header.operate()) {
                            case ADD: {
                                requestBuilder.addHeader(apacheHeader);
                                break;
                            }
                            case SET: {
                                requestBuilder.setHeader(apacheHeader);
                                break;
                            }
                            case REMOVE: {
                                requestBuilder.removeHeader(apacheHeader);
                                break;
                            }
                        }
                    }

                    if (args != null && args.length == 1 && args[0].getClass().isAssignableFrom(RestBody.class)) {
                        RestBody message = (RestBody) args[0];

                        if (message.isNull()) {
                            throw new NullPointerException(method + " - request message is null");
                        }

                        try {
                            requestBuilder.setEntity(new StringEntity(message.getMessage()));
                        }
                        catch (UnsupportedEncodingException exception) {
                            return finallyException(proxy, exception);
                        }
                    }

                    CloseableHttpResponse apacheResponse = httpClient.execute(requestBuilder.build());
                    return makeMethodResponse(proxy, apacheResponse);
                }
                catch (IOException exception) {
                    return finallyException(proxy, exception);
                }
            };

            if (RestUtilities.hasFlag(restFlagsArray, RestFlag.Type.ASYNC_REQUESTS)) {
                return CompletableFuture.supplyAsync(responseSupplier);
            }

            return CompletableFuture.completedFuture(responseSupplier.get());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public RestResponse makeResponse(Object proxy, CloseableHttpResponse apacheResponse) {
            try (InputStream inputStream = apacheResponse.getEntity().getContent()) {

                byte[] arr = new byte[(int) apacheResponse.getEntity().getContentLength()];
                inputStream.read(arr);

                return Responses.fromMessage(apacheResponse.getStatusLine().getStatusCode(), new String(arr, 0, arr.length));
            }
            catch (Exception exception) {
                return finallyException(proxy, exception);
            }
        }

        public Object makeMethodResponse(Object proxy, CloseableHttpResponse apacheResponse) {
            RestResponse response = makeResponse(proxy, apacheResponse);

            Class<?> returnType = this.method.getReturnType();

            if (!returnType.isAssignableFrom(RestResponse.class)) {
                return response.getBody().getAsJsonObject(returnType);
            }

            return response;
        }
    }

}
