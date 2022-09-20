package com.itzstonlex.restframework.api;

import java.lang.annotation.*;

/**
 * Marks the designated type
 * for automatic lookup of REST services.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestService {
}
