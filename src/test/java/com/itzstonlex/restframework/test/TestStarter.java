package com.itzstonlex.restframework.test;

import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestFrameworkStorage;
import com.itzstonlex.restframework.api.RestResponse;

public class TestStarter {

    public static void main(String[] args) {
        RestFrameworkStorage restStorage = RestFrameworkBootstrap.runServices(TestStarter.class);

        TestRestService testRestService = restStorage.get(TestRestService.class);

        Userdata meelad = testRestService.getUserdata("meelad");
        RestResponse meeladNative = testRestService.getUserdataResponse("meelad");

        System.out.println(meelad);
        System.out.println(meeladNative);
    }
}
