package com.itzstonlex.restframework.api.context.response;

import com.itzstonlex.restframework.api.context.RestBody;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

public class MessageResponse extends RestResponse {

    public static Response createResponse(String message) {
        return new Response(message);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Response {

        String message;
    }

    MessageResponse(int statusCode, String message) {
        super(statusCode, Responses.toStatusPhrase(statusCode), RestBody.fromString(message));
    }
}
