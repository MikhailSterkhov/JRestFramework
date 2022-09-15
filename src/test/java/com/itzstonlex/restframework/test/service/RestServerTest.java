package com.itzstonlex.restframework.test.service;

import com.itzstonlex.restframework.api.*;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.method.Post;
import com.itzstonlex.restframework.api.request.RestRequestMessage;
import com.itzstonlex.restframework.api.response.RestResponse;
import com.itzstonlex.restframework.test.Userdata;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestService
@RestServer(host = "localhost", port = 8082, defaultContext = "/api")
@RestFlag(RestFlag.Type.ASYNC_REQUESTS)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestServerTest {

    private List<Userdata> userdataList;

    @RestExceptionHandler
    public void onExceptionThrow(IOException exception) {
        System.out.println("HANDLE EXCEPTION!");

        exception.printStackTrace();
    }

    @Get(context = "/users")
    public RestResponse onUsersGet() {
        return RestResponse.createOnlyBody(200, userdataList);
    }

    @Get(context = "/users")
    public RestResponse onLimitedUsersGet(@RestParam("limit") long limit) {
        return RestResponse.createOnlyBody(200, userdataList.stream().limit(limit).collect(Collectors.toList()));
    }

    @Get(context = "/user")
    public RestResponse onUserGet(@RestParam("name") String name) {
        Userdata userdata = userdataList.stream()
                .filter(cached -> cached.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (userdata == null) {
            return RestResponse.create(404);
        }

        return RestResponse.createOnlyBody(200, userdata);
    }

    @Post(context = "/adduser")
    public RestResponse onUserAdd(@NonNull RestRequestMessage message) {
        Userdata newUserdata = message.getMessageAsJsonObject(Userdata.class);

        userdataList.add(newUserdata);

        message.setMessage("Successfully added");
        return RestResponse.createOnlyBody(200, message);
    }

}
