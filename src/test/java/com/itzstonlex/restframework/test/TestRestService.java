package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;

@RestService
@RestStructure(url = "https://api.agify.io", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @RestRequest(method = "GET", context = "/")
    Userdata getUserdata(@RestParam("name") String name);

    /**
     * And this function returns a direct HTTP result after
     * executing the request with all the native data
     *
     * @param name - Name of user.
     */
    @RestRequest(method = "GET", context = "/")
    RestResponse getUserdataResponse(@RestParam("name") String name);
}
