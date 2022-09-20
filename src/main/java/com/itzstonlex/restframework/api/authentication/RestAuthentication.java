package com.itzstonlex.restframework.api.authentication;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestAuthentication {

    String username();

    String password();
}
