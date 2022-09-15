package com.itzstonlex.restframework.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestServer {

    ProtocolType protocol() default ProtocolType.HTTP;

    String defaultContext() default "";

    String host();

    int port();

    int bindTimeout() default 2000;

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(makeFinal = true)
    enum ProtocolType {

        HTTP("http://"),
        HTTPS("https://"),
        ;

        private String prefix;
    }
}
