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

    public void set(Map<Class<?>, Object> foundedServicesMap) {
        this.foundedServicesMap.clear();
        this.foundedServicesMap.putAll(foundedServicesMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull Class<T> restType) {
        return (T) foundedServicesMap.get(restType);
    }

    @Override
    public void bind(@NonNull Class<?> restType, Object... initargs) {
        if (!foundedServicesMap.containsKey(restType)) {
            throw new IllegalArgumentException(restType + " is not marked @RestService");
        }

        RestUtilities.createServerProxy(restType, initargs);
    }
}
