package com.itzstonlex.restframework.test.reallife;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.api.RestOption;
import com.itzstonlex.restframework.api.RestService;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.context.response.RestResponse;

@RestService
@RestClient(url = "https://catfact.ninja")
@RestOption(RestOption.Type.ASYNCHRONOUS)
@RestOption(RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)
public interface CatFactsApi {

    @Get(context = "/fact")
    RestResponse getFactResponse();
}
