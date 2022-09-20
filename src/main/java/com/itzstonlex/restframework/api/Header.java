package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.api.repeatable.RepeatableHeaders;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableHeaders.class)
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
