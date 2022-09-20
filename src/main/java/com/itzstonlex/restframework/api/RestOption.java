package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.api.repeatable.RepeatableOptions;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RepeatableOptions.class)
public @interface RestOption {

    Type value();

    enum Type {

        DISALLOW_SIGNATURE,
        ASYNC_REQUESTS,
        THROW_UNHANDLED_EXCEPTIONS,
    }
}
