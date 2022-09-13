package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestFrameworkStorage;
import com.itzstonlex.restframework.api.RestRequestMessage;
import com.itzstonlex.restframework.api.RestResponse;

public class TestStarter {

    public static void main(String[] args) {
        RestFrameworkStorage restStorage = RestFrameworkBootstrap.runServices(TestStarter.class);

        TestRestService testRestService = restStorage.get(TestRestService.class);

        RestResponse entriesResponse = testRestService.postEntries(
                RestRequestMessage.asText("example text message")
        );

        System.out.println(entriesResponse.getResponseCode());
        System.out.println(entriesResponse.getResponseMessage());
        System.out.println(entriesResponse.getUrl());
        System.out.println(entriesResponse.getMethod());
        System.out.println(entriesResponse.getBody());
    }
}
