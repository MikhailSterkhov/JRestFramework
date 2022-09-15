package com.itzstonlex.restframework.api.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Put {

    String context();

    int timeout() default 2000;

    boolean useSignature() default true;
}
