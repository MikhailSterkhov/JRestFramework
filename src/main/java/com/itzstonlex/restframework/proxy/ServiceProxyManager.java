package com.itzstonlex.restframework.proxy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public final class ServiceProxyManager {

    private ProjectServicesScanner servicesScanner;

    public Map<Class<?>, Object> findServices(@NonNull String packageName) {
        Set<Class<?>> servicesSet = servicesScanner.findServices(packageName);

        Map<Class<?>, Object> servicesMap = new HashMap<>();
        servicesSet.forEach(serviceClass -> {

            Object proxyInstance = ServiceProxy.wrap(
                    servicesScanner.getBootstrapLoader(),
                    serviceClass
            );

            servicesMap.put(serviceClass, proxyInstance);
        });

        return servicesMap;
    }
}
