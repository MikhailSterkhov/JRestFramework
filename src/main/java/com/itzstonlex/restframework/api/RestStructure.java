package com.itzstonlex.restframework.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestStructure {

    EnumStructure struct() default EnumStructure.JSON;

    String url() default "";

    enum EnumStructure {

        JSON, XML, TEXT,
    }
}
