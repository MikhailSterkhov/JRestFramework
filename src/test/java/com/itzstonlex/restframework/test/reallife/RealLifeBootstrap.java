package com.itzstonlex.restframework.test.reallife;

import com.itzstonlex.restframework.RestFrameworkBootstrap;
import com.itzstonlex.restframework.RestServicePublicManager;

public class RealLifeBootstrap {

    public static void main(String[] args) {
        RestServicePublicManager rest = RestFrameworkBootstrap.runServices(RealLifeBootstrap.class);

        testAgify(rest);
        testCatFacts(rest);
        testGenderize(rest);
    }

    private static void testAgify(RestServicePublicManager rest) {
        System.out.println("\nAgifyApi:");

        AgifyApi agifyApi = rest.get(AgifyApi.class);

        System.out.println(agifyApi.getUserByName("Mikhail"));
        System.out.println(agifyApi.getUserResponse("Mikhail"));
        System.out.println(agifyApi.getUserByName("Mark").getName());
        System.out.println(agifyApi.getUserByName("johan").getAge());
    }

    private static void testCatFacts(RestServicePublicManager rest) {
        System.out.println("\nCatFactsApi:");

        CatFactsApi catFactsApi = rest.get(CatFactsApi.class);

        for (int i = 0; i < 10; i++) {
            System.out.println(catFactsApi.getFactResponse());
        }
    }

    private static void testGenderize(RestServicePublicManager rest) {
        System.out.println("\nGenderizeApi:");

        GenderizeApi genderizeApi = rest.get(GenderizeApi.class);

        System.out.println(genderizeApi.getGenderInfo("luc").getGender());
        System.out.println(genderizeApi.getGenderInfo("mark").getGender());
        System.out.println(genderizeApi.getGenderInfo("Milana").getGender());
        System.out.println(genderizeApi.getGenderInfo("alexandra").getGender());
    }

}
