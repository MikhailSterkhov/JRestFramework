package com.itzstonlex.restframework.test.reallife;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.api.RestParam;
import com.itzstonlex.restframework.api.RestService;
import com.itzstonlex.restframework.api.method.Get;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@RestService
@RestClient(url = "https://api.genderize.io")
public interface GenderizeApi {

    @Getter
    @ToString
    @RequiredArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    class GenderInfo {

        String gender;

        double probability;
    }

    @Get(context = "/")
    GenderInfo getGenderInfo(@RestParam("name") String name);
}
