package com.itzstonlex.restframework;

import com.itzstonlex.restframework.proxy.ProjectScanner;
import com.itzstonlex.restframework.proxy.ProxiedServiceManager;
import com.itzstonlex.restframework.proxy.RestServiceManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Getter
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestFrameworkBootstrap {

    /**
     * Starting automatic packet parsing services for
     * outgoing initialized Bootstrap.
     *
     * @param packageName - Name of project package.
     * @param classLoader - Outgoing bootstrap class loader.
     */
    public static RestServicePublicManager runServices(@NonNull String packageName, @NonNull ClassLoader classLoader) {
        RestFrameworkBootstrap restFramework = new RestFrameworkBootstrap(
                new RestServiceManager(new ProjectScanner(classLoader))
        );

        restFramework.initProjectServices(packageName);
        return restFramework.getPublicManager();
    }

    /**
     * Starting automatic packet parsing services for
     * outgoing initialized Bootstrap.
     *
     * @param bootstrapClass - Outgoing bootstrap class type.
     */
    public static RestServicePublicManager runServices(@NonNull Class<?> bootstrapClass) {
        return RestFrameworkBootstrap.runServices(bootstrapClass.getPackage().getName(), bootstrapClass.getClassLoader());
    }

    private Object lock = new Object();
    private RestServiceManager restServiceManager;

    @NonFinal
    private RestServicePublicManager publicManager;

    public void initProjectServices(@NonNull String packageName) {
        synchronized (lock) {

            ProxiedServiceManager proxiedServiceManager = restServiceManager.getProxiedServiceManager();
            proxiedServiceManager.set(
                    restServiceManager.findServices(packageName)
            );

            publicManager = proxiedServiceManager;
        }
    }

}
