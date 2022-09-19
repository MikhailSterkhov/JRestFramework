package com.itzstonlex.restframework.api.authentication;

public enum RestAuthResult {

    SUCCESS, FAILURE,
    ;

    public static RestAuthResult parse(boolean flag) {
        return flag ? SUCCESS : FAILURE;
    }
}
