package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.RestServicePublicManager;
import com.itzstonlex.restframework.util.RestUtilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.WeakHashMap;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ProxiedServiceManager implements RestServicePublicManager {

    private final Map<Class<?>, Object> foundedServicesMap = new WeakHashMap<>();

    /**
     * Initialize founded services.
     *
     * @param foundedServicesMap - Founded services table.
     */
    public void set(Map<Class<?>, Object> foundedServicesMap) {
        this.foundedServicesMap.clear();
        this.foundedServicesMap.putAll(foundedServicesMap);
    }

    /**
     * Getting automatically found in the package client services
     * marked with {@link com.itzstonlex.restframework.api.RestService} and
     * {@link com.itzstonlex.restframework.api.RestClient} annotations.
     *
     * @param restType - Type of service.
     * @return - Client Service instance.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull Class<T> restType) {
        return (T) foundedServicesMap.get(restType);
    }

    /**
     * Bind port of automatically found in the package client services
     * marked with {@link com.itzstonlex.restframework.api.RestService} and
     * {@link com.itzstonlex.restframework.api.RestServer} annotations.
     *
     * @param restType - Type of service.
     * @param initargs - Service type-constructor initial arguments.
     */
    @Override
    public void bind(@NonNull Class<?> restType, Object... initargs) {
        if (!foundedServicesMap.containsKey(restType)) {
            throw new IllegalArgumentException(restType + " is not marked @RestService");
        }

        RestUtilities.createServerProxy(restType, initargs);
    }
}
