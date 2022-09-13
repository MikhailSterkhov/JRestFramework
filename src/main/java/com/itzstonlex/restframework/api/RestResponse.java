package com.itzstonlex.restframework.api;

import com.google.gson.Gson;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.nio.charset.Charset;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestResponse {

    private static final Gson GSON = new Gson();

    private int responseCode;

    private String responseMessage;

    private String url;

    private String body;
    private String method;

    public byte[] getBodyAsByteArray() {
        return body.getBytes();
    }

    public byte[] getBodyAsByteArray(Charset charset) {
        return body.getBytes(charset);
    }

    public <T> T getBodyAsJsonObject(Class<T> type) {
        return GSON.fromJson(body, type);
    }
}
