package com.itzstonlex.restframework.proxy.type;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.context.RestBody;
import com.itzstonlex.restframework.api.context.request.RestRequestSignature;
import com.itzstonlex.restframework.api.context.response.Responses;
import com.itzstonlex.restframework.api.context.response.RestResponse;
import com.itzstonlex.restframework.util.RestUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@FieldDefaults(makeFinal = true)
public class ClientProxy implements InvocationHandler {

    private static final ExecutorService THREADS_POOL = Executors.newCachedThreadPool();

    /**
     * Wrapping the client REST service in Proxy
     * and initializing the finished instance.
     *
     * @param classLoader - REST Server-Service type.
     * @param providerInterface - Client-Service interface type.
     *
     * @return - Proxied server instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(ClassLoader classLoader, Class<T> providerInterface) {
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{providerInterface},
                new ClientProxy(providerInterface));
    }

    private Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap = new HashMap<>();
    private Map<String, ExecutableMethod> executionsMap = new HashMap<>();

    private CloseableHttpClient httpClient;

    @NonFinal
    private UsernamePasswordCredentials credentials;

    private ClientProxy(Class<?> interfaceClass) {
        RestClient restClient = RestUtilities.getClientAnnotation(interfaceClass);
        RestOption[] restFlagsArray = RestUtilities.getFlagsAnnotations(interfaceClass);

        if (restClient == null) {
            throw new RuntimeException("Annotation @RestClient not found for " + interfaceClass);
        }

        httpClient = HttpClientBuilder.create().build();

        if (interfaceClass.isAnnotationPresent(RestAuthentication.class)) {

            RestAuthentication authentication = interfaceClass.getDeclaredAnnotation(RestAuthentication.class);
            credentials = new UsernamePasswordCredentials(
                    RestUtilities.parseSystemProperties(authentication.username()),
                    RestUtilities.parseSystemProperties(authentication.password()));
        }

        for (Method method : interfaceClass.getDeclaredMethods()) {

            if (RestUtilities.checkAndSaveExceptionHandler(method, exceptionHandlersMap)) {
                continue;
            }

            RestRequestSignature request = RestUtilities.createRequestSignature(method);
            executionsMap.put(method.toString(),
                    new ExecutableMethod(restClient,

                            RestUtilities.getOptionsTypes(restFlagsArray),
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

        private RestClient clientAnnotation;

        private RestOption.Type[] options;
        private Header[] headers;

        private RestRequestSignature signature;

        private Method declaredMethod;

        /**
         * Handling incoming exceptions on the client side and using
         * proxied methods and setting flags on the part
         * of the interface developer.
         *
         * @param proxy - Client-service proxy instance.
         * @param exception - Throwing exception.
         */
        private RestResponse finallyException(Object proxy, Exception exception) {
            Throwable lastCause = RestUtilities.getLastCause(exception);

            if (!RestUtilities.handleException(proxy, lastCause, exceptionHandlersMap)) {

                if (RestUtilities.containsOption(options, RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)) {
                    lastCause.printStackTrace();
                }
            }

            return Responses.ofText(Responses.INTERNAL_SERVER_ERROR, lastCause.getMessage());
        }

        /**
         * Building a simple wrapped response to a
         * request based on the ratio of the parameters
         * that come to us.
         *
         * @param apacheResponse - Response from Apache HTTP client.
         * @return - Wrapped response data.
         */
        public RestResponse makeResponse(CloseableHttpResponse apacheResponse)
        throws Exception {

            HttpEntity httpEntity = apacheResponse.getEntity();

            int statusCode = apacheResponse.getStatusLine().getStatusCode();
            String statusEntity = EntityUtils.toString(httpEntity);

            return Responses.of(statusCode, RestBody.fromString(statusEntity));
        }

        /**
         * Building and converting the received response from the server
         * into the return type required from the declared method.
         *
         * @param apacheResponse - Response from Apache HTTP client.
         * @return - Converted response instance.
         */
        public Object makeMethodResponse(CloseableHttpResponse apacheResponse)
        throws Exception {

            RestResponse response = makeResponse(apacheResponse);

            Class<?> returnType = declaredMethod.getReturnType();

            if (!returnType.isAssignableFrom(RestResponse.class)) {
                return response.getBody().convert(returnType);
            }

            return response;
        }

        /**
         * Executing a HTTP proxied client request by
         * interface method signature parameters.
         *
         * @param proxy - Client-service proxy instance.
         * @param args - Values array of URL parameters.
         */
        public CompletableFuture<Object> execute(Object proxy, Object[] args) {
            Supplier<Object> responseSupplier = () -> {

                String fullLink = clientAnnotation.url() + signature.getUri();

                if (!RestUtilities.containsOption(options, RestOption.Type.DISALLOW_SIGNATURE) && signature.isUseSignature()) {
                    try {
                        fullLink += RestUtilities.makeLinkSignature(declaredMethod, args);
                    }
                    catch (UnsupportedEncodingException exception) {
                        return finallyException(proxy, exception);
                    }
                }

                try {
                    RequestBuilder requestBuilder = RequestBuilder.copy(new BasicHttpRequest(signature.getMethod(), fullLink));

                    if (credentials != null) {

                        String encoding = Base64.getEncoder().encodeToString((credentials.getUserName() + ":" + credentials.getPassword()).getBytes());
                        requestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
                    }

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
                        RestBody restBody = ((RestBody) args[0]);

                        if (restBody.isNull()) {
                            throw new NullPointerException(declaredMethod + " - request message is null");
                        }

                        requestBuilder.setEntity(new StringEntity(restBody.getMessage()));
                    }

                    try (CloseableHttpResponse apacheResponse = httpClient.execute(requestBuilder.build())) {
                        return makeMethodResponse(apacheResponse);
                    }
                }
                catch (Exception exception) {
                    return finallyException(proxy, exception);
                }
            };

            if (RestUtilities.containsOption(options, RestOption.Type.ASYNCHRONOUS)) {
                return CompletableFuture.supplyAsync(responseSupplier, THREADS_POOL);
            }

            return CompletableFuture.completedFuture(responseSupplier.get());
        }
    }

}
