package com.itzstonlex.restframework;

import lombok.NonNull;

public interface RestServicePublicManager {

    /**
     * Getting automatically found in the package client services
     * marked with {@link com.itzstonlex.restframework.api.RestService} and
     * {@link com.itzstonlex.restframework.api.RestClient} annotations.
     *
     * @param restType - Type of service.
     * @return - Client Service instance.
     */
    <T> T get(@NonNull Class<T> restType);

    /**
     * Bind port of automatically found in the package client services
     * marked with {@link com.itzstonlex.restframework.api.RestService} and
     * {@link com.itzstonlex.restframework.api.RestServer} annotations.
     *
     * @param restType - Type of service.
     * @param initargs - Service type-constructor initial arguments.
     */
    void bind(@NonNull Class<?> restType, Object... initargs);

}
