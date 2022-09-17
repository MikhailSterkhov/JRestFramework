package com.itzstonlex.restframework.test.reallife;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.api.RestFlag;
import com.itzstonlex.restframework.api.RestService;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.response.RestResponse;

@RestService
@RestClient(url = "https://catfact.ninja")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
@RestFlag(RestFlag.Type.THROW_UNHANDLED_EXCEPTIONS)
public interface CatFactsApi {

    @Get(context = "/fact")
    RestResponse getFactResponse();
}
