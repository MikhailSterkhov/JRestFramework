package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.api.RestClient;
import com.itzstonlex.restframework.util.RestUtilities;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public final class RestServiceProxyManager {

    private ProjectScanner projectScanner;

    public Map<Class<?>, Object> findServices(@NonNull String packageName) {
        Set<Class<?>> servicesSet = projectScanner.scanPackage(packageName);

        Map<Class<?>, Object> servicesMap = new HashMap<>();
        servicesSet.forEach(serviceClass -> {

            Object proxyInstance = null;

            if (serviceClass.isAnnotationPresent(RestClient.class)) {

                proxyInstance = RestUtilities.createClientProxy(
                        projectScanner.getBootstrapLoader(),
                        serviceClass
                );
            }

            // todo - maybe any more rest service?

            if (proxyInstance != null) {
                servicesMap.put(serviceClass, proxyInstance);
            }
        });

        return servicesMap;
    }
}
