package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestFrameworkStorage;
import com.itzstonlex.restframework.api.RestResponse;

import java.util.UUID;

public class TestStarter {

    public static void main(String[] args) {
        RestFrameworkStorage restStorage = RestFrameworkBootstrap.runServices(TestStarter.class);

        TestRestService testRestService = restStorage.get(TestRestService.class);

        RestResponse deleteResponse = testRestService.deleteUser(1);

        System.out.println(deleteResponse.getStatusCode());
        System.out.println(deleteResponse.getUrl());

        System.out.println(testRestService.getUsers().getUrl());
        System.out.println(testRestService.addUser(UUID.randomUUID(), "itzstonlex").getUrl());
    }
}
