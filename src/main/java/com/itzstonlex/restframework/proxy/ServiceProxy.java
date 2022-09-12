package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.api.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ServiceProxy implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static <T> T wrap(ClassLoader classLoader, Class<T> providerInterface) {
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{providerInterface}, new ServiceProxy());
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

        validateMethodAccess(method);

        RestStructure restStructure = getStructure(method);
        RestFlag[] restFlagsArray = getFlags(method);

        RestRequest restRequest = method.getDeclaredAnnotation(RestRequest.class);

        String fullLink = restStructure.url() + restRequest.context();
        if (restRequest.useSignature()) {
            fullLink += makeLinkSignature(method, args);
        }

        // todo - 13.09.2022 - Make URL request

        return new RestResponse(200, fullLink, "", restRequest.method());
    }
}
