package com.itzstonlex.restframework.api.response;

import com.itzstonlex.restframework.api.RestBody;
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

    public static final int CLIENT_ERR = 400;

    public static final int SERVER_ERR = 500;

    public static RestResponse create(int statusCode) {
        return new RestResponse(statusCode, null, null, null, null);
    }

    public static RestResponse create(int statusCode, String responseMessage) {
        return new RestResponse(statusCode, responseMessage, null, null, null);
    }

    public static RestResponse create(int statusCode, String responseMessage, String url) {
        return new RestResponse(statusCode, responseMessage, url, null, null);
    }

    private static RestBody convertBody(Object bodyObject) {
        return RestBody.asText(bodyObject instanceof String ? bodyObject.toString() : RestUtilities.GSON.toJson(bodyObject));
    }

    public static RestResponse create(int statusCode, String responseMessage, String url, Object body) {
        return new RestResponse(statusCode, responseMessage, url, convertBody(body), null);
    }

    public static RestResponse create(int statusCode, String responseMessage, String url, Object body, String method) {
        return new RestResponse(statusCode, responseMessage, url, convertBody(body), method);
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
    private RestBody body;

    @NonFinal
    private String method;
}
