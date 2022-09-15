package com.itzstonlex.restframework.api;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultipleRestHeaders.class)
public @interface RestHeader {

    Operation operate() default Operation.SET;

    String name();

    String value();

    enum Operation {

        SET,
        ADD,
        REMOVE,
    }
}
