package com.itzstonlex.restframework.api.repeatable;

import com.itzstonlex.restframework.api.Header;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatableHeaders {

    Header[] value();
}
