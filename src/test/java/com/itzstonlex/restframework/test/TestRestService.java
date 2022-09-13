package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;

@RestService
@RestStructure(url = "https://api.publicapis.org", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    @RestRequest(method = "GET", context = "/entries", useSignature = false)
    RestResponse getEntries();
}
