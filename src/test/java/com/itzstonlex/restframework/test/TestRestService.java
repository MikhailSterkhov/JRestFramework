package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;

import java.util.UUID;

@RestService
@RestStructure(url = "localhost:8080", struct = RestStructure.EnumStructure.JSON)
@RestFlag(RestFlag.Type.ALLOW_SIGNATURE)
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface TestRestService {

    @RestRequest(method = "POST", context = "/users")
    RestResponse addUser(
            @RestParam("uuid") UUID uuid,
            @RestParam("name") String username
    );

    @RestRequest(method = "DELETE", context = "/users")
    RestResponse deleteUser(
            @RestParam("id") long id
    );

    @RestRequest(method = "GET", context = "/users", useSignature = false)
    RestResponse getUsers();
}
