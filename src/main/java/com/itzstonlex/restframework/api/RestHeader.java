package com.itzstonlex.restframework.api;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultipleRestHeaders.class)
public @interface RestHeader {

    String name();

    String value();
}
