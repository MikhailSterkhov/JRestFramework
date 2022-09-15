package com.itzstonlex.restframework.api.response;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.nio.charset.Charset;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestResponse {

// ===================================================== //

    public static final int INFO = 100;
    public static final int SUCCESS = 200;
    public static final int REDIRECT = 300;
    public static final int CLIENT_ERROR = 400;
    public static final int SERVER_ERROR = 500;

    public static RestResponse create(int statusCode) {
        return new RestResponse(statusCode, null, null, null, null);
    }

    public static RestResponse create(int statusCode, String responseMessage) {
        return new RestResponse(statusCode, responseMessage, null, null, null);
    }

    public static RestResponse create(int statusCode, String responseMessage, String url) {
        return new RestResponse(statusCode, responseMessage, url, null, null);
    }

    public static RestResponse create(int statusCode, String responseMessage, String url, Object body) {
        String bodyMessage = body instanceof String ? body.toString() : RestUtilities.GSON.toJson(body);
        return new RestResponse(statusCode, responseMessage, url, bodyMessage, null);
    }

    public static RestResponse create(int statusCode, String responseMessage, String url, Object body, String method) {
        String bodyMessage = body instanceof String ? body.toString() : RestUtilities.GSON.toJson(body);
        return new RestResponse(statusCode, responseMessage, url, bodyMessage, method);
    }

    public static RestResponse createOnlyBody(int statusCode, Object body) {
        return create(statusCode, null, null, body, null);
    }

// ===================================================== //

    private int statusCode;
    private String statusMessage;

    @NonFinal
    private String url;

    @NonFinal
    private String body;

    @NonFinal
    private String method;

    public byte[] getBodyAsByteArray() {
        return body.getBytes();
    }

    public byte[] getBodyAsByteArray(Charset charset) {
        return body.getBytes(charset);
    }

    public <T> T getBodyAsJsonObject(Class<T> type) {
        try {
            return RestUtilities.GSON.fromJson(body, type);
        }
        catch (Exception exception) {
            return null;
        }
    }
}
