package com.itzstonlex.restframework.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestResponse {

    private int statusCode;

    private String url;
    private String body;
    private String method;
}
