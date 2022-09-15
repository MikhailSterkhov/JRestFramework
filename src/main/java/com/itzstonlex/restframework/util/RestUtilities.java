package com.itzstonlex.restframework.util;

import com.google.gson.Gson;
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.*;
import com.itzstonlex.restframework.api.request.RestRequest;
import com.itzstonlex.restframework.proxy.type.ClientProxy;
import com.itzstonlex.restframework.proxy.type.ServerProxy;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@UtilityClass
public class RestUtilities {

    public final Gson GSON = new Gson();

    private final Map<Class<? extends Annotation>, Function<Annotation, RestRequest>> REQUEST_ANNOTATIONS_TYPES
            = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> void addRequestAnnotationType(Class<T> cls, Function<T, RestRequest> function) {
        REQUEST_ANNOTATIONS_TYPES.put(cls, (Function<Annotation, RestRequest>) function);
    }

    static {
        RestUtilities.addRequestAnnotationType(Delete.class, annotation -> new RestRequest("DELETE", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Get.class, annotation -> new RestRequest("GET", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Post.class, annotation -> new RestRequest("POST", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Put.class, annotation -> new RestRequest("PUT", annotation.context(), annotation.timeout(), annotation.useSignature()));
    }

    public Set<Class<? extends Annotation>> getRequestAnnotationsTypes() {
        return Collections.unmodifiableSet(REQUEST_ANNOTATIONS_TYPES.keySet());
    }

    public <T extends Annotation> RestRequest newRequestByAnnotationType(@NonNull T annotation) {
        return REQUEST_ANNOTATIONS_TYPES.get(annotation.annotationType()).apply(annotation);
    }

    public RestRequest newRequestByAnnotationType(@NonNull RequestMethod requestMethod) {
        return new RestRequest(requestMethod.method(), requestMethod.context(),
                requestMethod.timeout(), requestMethod.useSignature());
    }

    public <T> T createServerProxy(@NonNull Class<T> cls, Object... initargs) {
        Class<?>[] initargsTypes = null;

        if (initargs.length > 0) {
            for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
                if (constructor.getParameterCount() != initargs.length) {
                    continue;
                }

                Parameter[] parameters = constructor.getParameters();
                boolean initialized = false;

                int counter = 0;
                for (Parameter parameter : parameters) {
                    Object arg = initargs[counter++];

                    if (!parameter.getType().isAssignableFrom(arg.getClass())) {
                        break;
                    }

                    if (counter >= parameters.length) {
                        initialized = true;
                    }
                }

                if (initialized) {
                    initargsTypes = Arrays.stream(parameters).map(Parameter::getType).toArray(Class[]::new);
                    break;
                }
            }

            if (initargsTypes == null) {
                initargsTypes = Arrays.stream(initargs).map(Object::getClass).toArray(Class[]::new);
            }
        }
        else {
            initargsTypes = new Class[0];
        }

        return ServerProxy.wrap(cls, initargsTypes, initargs);
    }

    public <T> T createClientProxy(@NonNull ClassLoader classLoader, @NonNull Class<T> cls) {
        return ClientProxy.wrap(classLoader, cls);
    }

    public RestFlag.Type[] getRestFlagsTypes(RestFlag[] restFlagsArray) {
        return Arrays.stream(restFlagsArray).map(RestFlag::value).toArray(RestFlag.Type[]::new);
    }

    public RestServer getServerAnnotation(Class<?> declaringClass) {
        Class<RestServer> annotationType = RestServer.class;

        if (!declaringClass.isAnnotationPresent(annotationType)) {
            throw new RuntimeException(declaringClass + " is not annotation @RestServer present");
        }

        return declaringClass.getDeclaredAnnotation(annotationType);
    }

    public RestClient getClientAnnotation(Class<?> declaringClass) {
        Class<RestClient> annotationType = RestClient.class;

        if (!declaringClass.isAnnotationPresent(annotationType)) {
            throw new RuntimeException(declaringClass + " is not annotation @RestClient present");
        }

        return declaringClass.getDeclaredAnnotation(annotationType);
    }

    public RestFlag[] getFlagsAnnotations(Class<?> declaringClass) {
        Class<MultipleRestFlags> annotationType = MultipleRestFlags.class;

        if (!declaringClass.isAnnotationPresent(annotationType)) {
            return new RestFlag[0];
        }

        return declaringClass.getDeclaredAnnotation(annotationType).value();
    }

    public RestHeader[] getHeaders(Method method) {
        Class<MultipleRestHeaders> annotationType = MultipleRestHeaders.class;

        if (!method.isAnnotationPresent(annotationType)) {
            return new RestHeader[0];
        }

        return method.getDeclaredAnnotation(annotationType).value();
    }

    public String makeLinkSignature(Method method, Object[] args)
    throws UnsupportedEncodingException {

        StringBuilder stringBuilder = new StringBuilder("?");
        Parameter[] parameters = method.getParameters();

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];

            if (parameter.getType().isAssignableFrom(RestBody.class)) {
                continue;
            }

            if (index > 0) {
                stringBuilder.append("&");
            }

            String paramName = parameter.isAnnotationPresent(RestParam.class)
                    ? parameter.getDeclaredAnnotation(RestParam.class).value()
                    : parameter.getName();

            stringBuilder.append(paramName).append("=")
                    .append(URLEncoder.encode(args[index].toString(), StandardCharsets.UTF_8.name()));
        }

        return stringBuilder.toString();
    }

    @SneakyThrows
    public void handleException(Object source, Throwable throwable, Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap) {
        if (exceptionHandlersMap.isEmpty()) {
            throwable.printStackTrace();
            return;
        }

        boolean founded = false;
        Set<Class<? extends Throwable>> exceptionClasses = exceptionHandlersMap.keySet();

        for (Class<? extends Throwable> exceptionType : exceptionClasses) {

            if (exceptionType.isAssignableFrom(throwable.getClass())) {

                for (Method exceptionHandler : exceptionHandlersMap.get(exceptionType)) {
                    exceptionHandler.invoke(source, throwable);

                    founded = true;
                }
            }
        }

        if (!founded) {
            List<Method> methods = exceptionHandlersMap.get(throwable.getClass());

            if (methods != null) {

                for (Method exceptionHandler : methods) {
                    exceptionHandler.invoke(source, throwable);
                }
            }
        }
    }

    public boolean hasFlag(RestFlag.Type[] restFlagsTypes, RestFlag.Type flag) {
        return Arrays.asList(restFlagsTypes).contains(flag);
    }

}
