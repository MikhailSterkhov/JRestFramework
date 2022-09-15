package com.itzstonlex.restframework.test.service;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.RestBody;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.test.Userdata;

import java.io.IOException;
import java.util.List;

@RestService
@RestClient(url = "http://localhost:8082/api")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
public interface RestClientTest {

    /**
     * Handling of exceptions.
     *
     * @param exception - Thrown exception.
     */
    @RestExceptionHandler
    void handle(IOException exception);

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @Get(context = "/user")
    Userdata getUserdata(@RestParam("name") String name);

    @Get(context = "/users")
    List<Userdata> getCachedUserdataList(@RestParam("limit") long limit);

    /**
     * And this function returns a direct HTTP result after
     * executing the request with all the native data
     *
     * @param name - Name of user.
     */
    @Get(context = "/user")
    RestResponse getUserdataAsResponse(@RestParam("name") String name);

    /**
     * The requestMethod body can be created using the
     * {@link RestBody}
     * factory, as shown in this example
     */
    @RestHeader(name = "Content-Type", value = "application/json", operate = RestHeader.Operation.ADD)
    @RestHeader(name = "Auth-Token", value = "testToken")
    @Post(context = "/adduser", useSignature = false)
    RestResponse addUserdata(@RestParam RestBody postMessage);
}
