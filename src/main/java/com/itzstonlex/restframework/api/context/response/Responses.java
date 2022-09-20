package com.itzstonlex.restframework.api.context.response;

import com.itzstonlex.restframework.api.context.RestBody;
import lombok.experimental.UtilityClass;
import org.apache.http.impl.EnglishReasonPhraseCatalog;

import java.util.Locale;

/**
 * This class is a factory for
 * creating the simplest responses,
 * their implementations and parameters
 */
@UtilityClass
public final class Responses {

    // HTTP CONTINUE CODE.
    public final int CONTINUE = 100;

    // HTTP OK CODE.
    public final int OK = 200;

    // HTTP MULTIPLE_CHOICES CODE.
    public final int MULTIPLE_CHOICES = 300;

    // HTTP BAD_REQUEST CODE.
    public final int BAD_REQUEST = 400;

    // HTTP INTERNAL_SERVER_ERROR CODE.
    public final int INTERNAL_SERVER_ERROR = 500;

    /**
     * Parsing HTTP code to response message.
     *
     * @param statusCode - HTTP code.
     * @return - Response phrase message.
     */
    public String toStatusPhrase(int statusCode) {
        return EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.getDefault());
    }

    /**
     * Creating simplest response data
     *
     * @param concat - HTTP code designation with tag: OK.
     * @return - Wrapped response data.
     */
    public RestResponse asSuccess(int concat) {
        return ofNullable(OK + concat);
    }

    /**
     * Creating simplest response data
     *
     * @param concat - HTTP code designation with tag: BAD_REQUEST.
     * @return - Wrapped response data.
     */
    public RestResponse asClientError(int concat) {
        return ofNullable(BAD_REQUEST + concat);
    }

    /**
     * Creating simplest response data
     *
     * @param concat - HTTP code designation with tag: INTERNAL_SERVER_ERROR.
     * @return - Wrapped response data.
     */
    public RestResponse asServerError(int concat) {
        return ofNullable(INTERNAL_SERVER_ERROR + concat);
    }

    /**
     * Creating simplest response data
     *
     * @param statusCode - HTTP response code.
     * @return - Wrapped response data.
     */
    public RestResponse ofNullable(int statusCode) {
        return new RestResponse(statusCode, toStatusPhrase(statusCode), null);
    }

    /**
     * Creating simplest response data
     *
     * @param statusCode - HTTP response code.
     * @param body - Wrapped body object of HTTP response.
     *
     * @return - Wrapped response data.
     */
    public RestResponse of(int statusCode, RestBody body) {
        return new RestResponse(statusCode, toStatusPhrase(statusCode), body);
    }

    /**
     * Creating a JSON response data
     *
     * @param statusCode - HTTP response code.
     * @param object - Object has json conversion.
     *
     * @return - Wrapped response data.
     */
    public JsonResponse ofJSON(int statusCode, Object object) {
        return new JsonResponse(statusCode, object);
    }

    /**
     * Creating simplest response data
     *
     * @param statusCode - HTTP response code.
     * @param text - Text message of HTTP response body.
     *
     * @return - Wrapped response data.
     */
    public MessageResponse ofText(int statusCode, String text) {
        return new MessageResponse(statusCode, text);
    }

    /**
     * Creating simplest response data
     *
     * @param statusCode - HTTP response code.
     * @param json - JSON message of HTTP response body.
     *
     * @return - Wrapped response data.
     */
    public JsonResponse ofJSONMessage(int statusCode, String json) {
        return ofJSON(statusCode, MessageResponse.createResponse(json));
    }
}
