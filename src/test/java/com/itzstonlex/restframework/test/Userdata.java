package com.itzstonlex.restframework.test;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Userdata {

    private final String name;

    private final int age;
    private final int count;
}
