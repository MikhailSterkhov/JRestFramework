package com.itzstonlex.restframework.test.reallife;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.api.RestOption;
import com.itzstonlex.restframework.api.RestParam;
import com.itzstonlex.restframework.api.RestService;
import com.itzstonlex.restframework.api.method.Get;
import com.itzstonlex.restframework.api.context.response.RestResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@RestService
@RestClient(url = "https://api.agify.io")
@RestOption(RestOption.Type.ASYNCHRONOUS)
@RestOption(RestOption.Type.THROW_UNHANDLED_EXCEPTIONS)
public interface AgifyApi {

    @Getter
    @ToString
    @RequiredArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    class User {

        String name;

        int age;
        int count;
    }

    @Get(context = "/", timeout = 100)
    User getUserByName(@RestParam("name") String name);

    @Get(context = "/", timeout = 100)
    RestResponse getUserResponse(@RestParam("name") String name);
}
