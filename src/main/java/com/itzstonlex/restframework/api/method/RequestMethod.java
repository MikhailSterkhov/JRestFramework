package com.itzstonlex.restframework.api.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMethod {

    String method();

    String context();

    int timeout() default 2000;

    boolean useSignature() default true;
}
