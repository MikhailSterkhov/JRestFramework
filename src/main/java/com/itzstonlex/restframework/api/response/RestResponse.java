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

    public static RestResponse create(int responseCode) {
        return new RestResponse(responseCode, null, null, null, null);
    }

    public static RestResponse create(int responseCode, String responseMessage) {
        return new RestResponse(responseCode, responseMessage, null, null, null);
    }

    public static RestResponse create(int responseCode, String responseMessage, String url) {
        return new RestResponse(responseCode, responseMessage, url, null, null);
    }

    public static RestResponse create(int responseCode, String responseMessage, String url, Object body) {
        String bodyMessage = body instanceof String ? body.toString() : RestUtilities.GSON.toJson(body);
        return new RestResponse(responseCode, responseMessage, url, bodyMessage, null);
    }

    public static RestResponse create(int responseCode, String responseMessage, String url, Object body, String method) {
        String bodyMessage = body instanceof String ? body.toString() : RestUtilities.GSON.toJson(body);
        return new RestResponse(responseCode, responseMessage, url, bodyMessage, method);
    }

    public static RestResponse createOnlyBody(int responseCode, Object body) {
        return create(responseCode, null, null, body, null);
    }

// ===================================================== //

    private int responseCode;
    private String responseMessage;

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
