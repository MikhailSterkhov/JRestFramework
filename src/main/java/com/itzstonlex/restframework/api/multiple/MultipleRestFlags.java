package com.itzstonlex.restframework.api.multiple;

import com.itzstonlex.restframework.api.RestFlag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipleRestFlags {

    RestFlag[] value();
}
