package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.authentication.RestAuthResult;
import com.itzstonlex.restframework.api.authentication.RestAuthentication;
import com.itzstonlex.restframework.api.authentication.RestAuthenticationResult;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.request.RestRequestContext;
import com.itzstonlex.restframework.api.response.Responses;
import com.itzstonlex.restframework.api.response.RestResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

@RestService
@RestServer(host = "localhost", port = 8082, defaultContext = "/api")
@RestAuthentication(username = "admin", password = "password")
@RestFlag(RestFlag.Type.THROW_UNHANDLED_EXCEPTIONS)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestServerTest {

    private static final int NOT_FOUND_ERR = (Responses.BAD_REQUEST + 4);

    private static final String TOKEN_HEADER = "Auth-Token",
                                TOKEN = "TestToken123";

    private List<Userdata> userdataList;

    @Get(context = "/users", timeout = 200)
    public RestResponse onUsersGet() {
        return Responses.fromJSON(Responses.OK, userdataList);
    }

    @Get(context = "/users")
    public RestResponse onLimitedUsersGet(@RestParam("limit") long limit) {
        return Responses.fromJSON(Responses.OK, userdataList.stream().limit(limit).collect(Collectors.toList()));
    }

    @Get(context = "/user")
    public RestResponse onUserGet(@RestParam("name") String name) {
        Userdata userdata = userdataList.stream()
                .filter(cached -> cached.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (userdata == null) {
            return Responses.fromMessage(NOT_FOUND_ERR, "Userdata is not found");
        }

        return Responses.fromJSON(Responses.OK, userdata);
    }

    @Post(context = "/adduser", timeout = 250)
    public RestResponse onUserAdd(@RestParam RestRequestContext context) {
        String tokenHeader = context.getFirstHeader(TOKEN_HEADER);

        if (tokenHeader == null || !tokenHeader.equals(TOKEN)) {
            throw new IllegalArgumentException(TOKEN_HEADER);
        }

        RestBody message = context.getBody();

        Userdata newUserdata = message.getAsJsonObject(Userdata.class);
        userdataList.add(newUserdata);

        return Responses.fromMessageAsJSON(Responses.OK, "Successfully added");
    }

    @RestExceptionHandler
    public void onExceptionThrow(IllegalArgumentException exception) {
        System.out.println("Wrong authentication token!");
    }

    @RestAuthenticationResult
    public void onAuthResult(RestAuthResult result) {
        switch (result) {

            case SUCCESS: {
                System.out.println("[Server] Success authorized!");
                break;
            }

            case FAILURE: {
                System.out.println("[Server] Forbidden");
                break;
            }
        }
    }

}
