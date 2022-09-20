package com.itzstonlex.restframework.api.authentication;

public enum RestAuthResult {

    /**
     * This enum-constant mark successful
     * HTTP request authorization. (200 OK)
     */
    SUCCESS,

    /**
     * This enum-constant mark failed
     * HTTP request authorization. (403 Forbidden)
     */
    FAILURE,
    ;

    public static RestAuthResult parse(boolean flag) {
        return flag ? SUCCESS : FAILURE;
    }
}
