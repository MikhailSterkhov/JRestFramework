package com.itzstonlex.restframework.api.response;

import com.itzstonlex.restframework.api.RestBody;
import org.apache.http.impl.EnglishReasonPhraseCatalog;

import java.util.Locale;

public final class Responses {

    public static final int CONTINUE = 100;

    public static final int OK = 200;

    public static final int MULTIPLE_CHOICES = 300;

    public static final int BAD_REQUEST = 400;

    public static final int INTERNAL_SERVER_ERROR = 500;

    public static String toStatusPhrase(int statusCode) {
        return EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.getDefault());
    }

    public static RestResponse asSuccess(int concat) {
        return simple(OK + concat);
    }

    public static RestResponse asClientError(int concat) {
        return simple(BAD_REQUEST + concat);
    }

    public static RestResponse asServerError(int concat) {
        return simple(INTERNAL_SERVER_ERROR + concat);
    }

    public static RestResponse simple(int statusCode) {
        return new RestResponse(statusCode, toStatusPhrase(statusCode), null);
    }

    public static RestResponse fromBody(int statusCode, RestBody body) {
        return new RestResponse(statusCode, toStatusPhrase(statusCode), body);
    }

    public static JsonResponse fromJSON(int statusCode, Object object) {
        return new JsonResponse(statusCode, object);
    }

    public static MessageResponse fromMessage(int statusCode, String message) {
        return new MessageResponse(statusCode, message);
    }

    public static JsonResponse fromMessageAsJSON(int statusCode, String message) {
        return fromJSON(statusCode, MessageResponse.createResponse(message));
    }
}
