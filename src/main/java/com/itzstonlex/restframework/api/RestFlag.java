package com.itzstonlex.restframework.api;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultipleServiceFlags.class)
public @interface RestFlag {

    Type value();

    enum Type {

        ALLOW_SIGNATURE,
        ASYNC_REQUESTS,
        ;
    }
}
