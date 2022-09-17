package com.itzstonlex.restframework.api.response;

import com.itzstonlex.restframework.util.RestUtilities;

public class JsonResponse extends MessageResponse {

    JsonResponse(int statusCode, Object obj) {
        super(statusCode, RestUtilities.GSON.toJson(obj));
    }
}
