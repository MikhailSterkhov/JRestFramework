package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.response.RestResponse;

import java.io.IOException;
import java.util.List;

@RestService
@RestClient(url = "http://localhost:8082/api")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
@RestFlag(RestFlag.Type.THROW_UNHANDLED_EXCEPTIONS)
public interface RestClientTest {

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (Userdata)
     *
     * @param name - Name of user.
     */
    @Get(context = "/user")
    @Header(name = "Content-Type", value = "application/json")
    Userdata getUserdata(@RestParam("name") String name);

    /**
     * This function automatically converts the received JSON into
     * the object specified in the return object type of this function (List)
     *
     * @param limit - Limit of users list size
     */
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
     * {@link com.itzstonlex.restframework.api.RestBody}
     * factory, as shown in this example
     */
    @Post(context = "/adduser", useSignature = false)
    @Header(name = "Content-Type", value = "application/json")
    @Header(name = "Auth-Token", value = "TestToken123", operate = Header.Operation.ADD)
    RestResponse addUserdata(@RestParam RestBody body);
}
