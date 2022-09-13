package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;
import lombok.NonNull;

@RestService
@RestStructure(url = "https://api.publicapis.org", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    @RestRequest(method = "GET", context = "/entries", useSignature = false)
    RestResponse getEntries();

    @RestRequest(method = "POST", context = "/entries", useSignature = false)
    RestResponse postEntries(@NonNull RestRequestMessage message);
}
