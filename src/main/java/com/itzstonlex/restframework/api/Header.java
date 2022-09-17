package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.api.multiple.MultipleHeaders;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultipleHeaders.class)
public @interface Header {

    Operation operate() default Operation.SET;

    String name();

    String value();

    enum Operation {

        SET,
        ADD,
        REMOVE,
    }
}
