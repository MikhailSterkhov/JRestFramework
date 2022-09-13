package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.api.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@FieldDefaults(makeFinal = true)
public class ServiceProxy implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(ClassLoader classLoader, Class<T> providerInterface) {
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{providerInterface}, new ServiceProxy(providerInterface));
    }

    private Map<String, ServiceProxyMethod> declaredProxyMethodsMap = new HashMap<>();

    public ServiceProxy(Class<?> interfaceClass) {

        for (Method method : interfaceClass.getDeclaredMethods()) {
            validateMethodAccess(method);

            RestStructure restStructure = getStructure(method);
            RestFlag[] restFlagsArray = getFlags(method);

            RestRequest restRequest = method.getDeclaredAnnotation(RestRequest.class);

            RestFlag.Type[] restFlagsTypesArray = Arrays.stream(restFlagsArray)
                    .map(RestFlag::value)
                    .toArray(RestFlag.Type[]::new);

            declaredProxyMethodsMap.put(method.toString(),
                    new ServiceProxyMethod(
                            restStructure, restFlagsTypesArray, restRequest, method
                    ));
        }
    }

    private void validateMethodAccess(Method method) {
        if (!method.getReturnType().isAssignableFrom(RestResponse.class)) {
            throw new RuntimeException(method + " return type is not assignable from RestResponse");
        }

        if (!method.isAnnotationPresent(RestRequest.class)) {
            throw new RuntimeException(method + " is not annotation @RestRequest present");
        }
    }

    private RestStructure getStructure(Method method) {
        Class<RestStructure> restStructureClass = RestStructure.class;
        Class<?> declaringClass = method.getDeclaringClass();

        if (!declaringClass.isAnnotationPresent(restStructureClass)) {
            throw new RuntimeException(declaringClass + " is not annotation @RestStructure present");
        }

        return declaringClass.getDeclaredAnnotation(restStructureClass);
    }

    private RestFlag[] getFlags(Method method) {
        Class<MultipleServiceFlags> multipleServiceFlagsClass = MultipleServiceFlags.class;
        Class<?> declaringClass = method.getDeclaringClass();

        if (!declaringClass.isAnnotationPresent(multipleServiceFlagsClass)) {
            return new RestFlag[0];
        }

        return declaringClass.getDeclaredAnnotation(multipleServiceFlagsClass).value();
    }

    private String makeLinkSignature(Method method, Object[] args)
    throws UnsupportedEncodingException {

        StringBuilder stringBuilder = new StringBuilder("?");
        Parameter[] parameters = method.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            if (index > 0) {
                stringBuilder.append("&");
            }

            Parameter parameter = parameters[index];

            String paramName = parameter.isAnnotationPresent(RestParam.class)
                    ? parameter.getDeclaredAnnotation(RestParam.class).value()
                    : parameter.getName();

            stringBuilder.append(paramName).append("=")
                    .append(URLEncoder.encode(args[index].toString(), StandardCharsets.UTF_8.name()));
        }

        return stringBuilder.toString();
    }

    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable {

        ServiceProxyMethod serviceProxyMethod = declaredProxyMethodsMap.get(method.toString());

        if (serviceProxyMethod != null) {
            return serviceProxyMethod.execute(args).join();
        }

        throw new UnsupportedOperationException();
    }

    @Getter
    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private class ServiceProxyMethod {

        private RestStructure restStructure;
        private RestFlag.Type[] restFlagsArray;

        private RestRequest restRequest;

        private Method method;

        private boolean hasFlag(RestFlag.Type flag) {
            return Arrays.asList(restFlagsArray).contains(flag);
        }

        public CompletableFuture<RestResponse> execute(Object[] args) {
            Supplier<RestResponse> responseSupplier = () -> {
                HttpURLConnection urlConnection = null;

                String fullLink = restStructure.url() + restRequest.context();

                if (hasFlag(RestFlag.Type.ALLOW_SIGNATURE) && restRequest.useSignature()) {
                    try {
                        fullLink += makeLinkSignature(method, args);
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }

                try {
                    urlConnection = (HttpURLConnection) new URL(fullLink).openConnection();

                    urlConnection.setRequestMethod(restRequest.method());

                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(restRequest.timeout());

                    if (args != null && args.length == 1 && args[0].getClass().isAssignableFrom(RestRequestContext.class)) {
                        // todo - request output logic with RestRequestContext
                        return makeResponse(urlConnection);
                    }
                    else {
                        return makeResponse(urlConnection);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            };

            if (hasFlag(RestFlag.Type.ASYNC_REQUESTS)) {
                return CompletableFuture.supplyAsync(responseSupplier);
            }

            return CompletableFuture.completedFuture(responseSupplier.get());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @SneakyThrows
        public RestResponse makeResponse(HttpURLConnection urlConnection) {
            try (InputStream inputStream = urlConnection.getInputStream()) {

                byte[] arr = new byte[inputStream.available()];
                inputStream.read(arr);

                return new RestResponse(
                        urlConnection.getResponseCode(),
                        urlConnection.getResponseMessage(),

                        urlConnection.getURL().toString(),

                        new String(arr, 0, arr.length, StandardCharsets.UTF_8),

                        urlConnection.getRequestMethod()
                );
            }
        }
    }

}
