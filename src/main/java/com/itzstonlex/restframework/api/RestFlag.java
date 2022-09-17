package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.api.multiple.MultipleRestFlags;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultipleRestFlags.class)
public @interface RestFlag {

    Type value();

    enum Type {

        DISALLOW_SIGNATURE,
        ASYNC_REQUESTS,
    }
}
