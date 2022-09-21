package com.itzstonlex.restframework.util;

import com.google.gson.Gson;
import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.context.RestBody;
import com.itzstonlex.restframework.api.method.*;
import com.itzstonlex.restframework.api.repeatable.RepeatableHeaders;
import com.itzstonlex.restframework.api.repeatable.RepeatableOptions;
import com.itzstonlex.restframework.api.context.request.RestRequestSignature;
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

    private final Map<Class<? extends Annotation>, Function<Annotation, RestRequestSignature>> REQUEST_ANNOTATIONS_TYPES
            = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> void addRequestAnnotationType(Class<T> cls, Function<T, RestRequestSignature> function) {
        REQUEST_ANNOTATIONS_TYPES.put(cls, (Function<Annotation, RestRequestSignature>) function);
    }

    static {
        RestUtilities.addRequestAnnotationType(Delete.class, annotation -> new RestRequestSignature("DELETE", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Get.class, annotation -> new RestRequestSignature("GET", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Post.class, annotation -> new RestRequestSignature("POST", annotation.context(), annotation.timeout(), annotation.useSignature()));
        RestUtilities.addRequestAnnotationType(Put.class, annotation -> new RestRequestSignature("PUT", annotation.context(), annotation.timeout(), annotation.useSignature()));
    }

    public Set<Class<? extends Annotation>> getRequestAnnotationsTypes() {
        return Collections.unmodifiableSet(REQUEST_ANNOTATIONS_TYPES.keySet());
    }

    public <T extends Annotation> RestRequestSignature newRequestByAnnotationType(@NonNull T annotation) {
        return REQUEST_ANNOTATIONS_TYPES.get(annotation.annotationType()).apply(annotation);
    }

    public boolean containsOption(RestOption.Type[] optionsArray, RestOption.Type target) {
        return Arrays.asList(optionsArray).contains(target);
    }

    public RestRequestSignature newRequestByAnnotationType(@NonNull Request request) {
        return new RestRequestSignature(request.method(), request.context(),
                request.timeout(), request.useSignature());
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

    public RestOption.Type[] getOptionsTypes(RestOption[] restFlagsArray) {
        return Arrays.stream(restFlagsArray).map(RestOption::value).toArray(RestOption.Type[]::new);
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

    public RestOption[] getFlagsAnnotations(Class<?> declaringClass) {
        Class<RepeatableOptions> multiAnnotationType = RepeatableOptions.class;
        Class<RestOption> singleAnnotationType = RestOption.class;

        if (!declaringClass.isAnnotationPresent(multiAnnotationType)) {
            if (!declaringClass.isAnnotationPresent(singleAnnotationType)) {
                return new RestOption[0];
            }

            return new RestOption[]{declaringClass.getDeclaredAnnotation(singleAnnotationType)};
        }

        return declaringClass.getDeclaredAnnotation(multiAnnotationType).value();
    }

    public Header[] getHeaders(Method method) {
        Class<RepeatableHeaders> annotationType = RepeatableHeaders.class;

        if (!method.isAnnotationPresent(annotationType)) {
            return new Header[0];
        }

        return method.getDeclaredAnnotation(annotationType).value();
    }

    public RestRequestSignature createRequestSignature(Method method) {
        RestRequestSignature request = null;
        Request requestMethod = method.getDeclaredAnnotation(Request.class);

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

        return request;
    }

    @SuppressWarnings("unchecked")
    public boolean checkAndSaveExceptionHandler(Method method, Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap) {
        if (method.isAnnotationPresent(RestExceptionHandler.class)) {

            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Exception handler " + method + " must be have only 1 Throwable superclass in signature");
            }

            Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) method.getParameters()[0].getType();

            List<Method> exceptionHandlers = exceptionHandlersMap.computeIfAbsent(exceptionType, k -> new ArrayList<>());
            exceptionHandlers.add(method);

            return true;
        }

        return false;
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

    public Throwable getLastCause(Throwable cause) {
        Throwable throwable = cause;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        return throwable;
    }

    @SneakyThrows
    public boolean handleException(Object source, Throwable throwable, Map<Class<? extends Throwable>, List<Method>> exceptionHandlersMap) {
        if (exceptionHandlersMap.isEmpty()) {
            return false;
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

        return founded;
    }

    public String parseSystemProperties(String string) {
        Properties properties = System.getProperties();

        Set<String> keys = properties.stringPropertyNames();

        for (String key : keys) {
            string = string.replace(String.format("${%s}", "system." + key), properties.getProperty(key));
        }

        return string;
    }

}
