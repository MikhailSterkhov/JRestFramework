package com.itzstonlex.restframework;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.WeakHashMap;

@FieldDefaults(makeFinal = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class RestFrameworkStorage {

    private Map<Class<?>, Object> servicesMap = new WeakHashMap<>();

    void addServices(Map<Class<?>, Object> servicesMap) {
        this.servicesMap.putAll(servicesMap);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        return (T) servicesMap.get(serviceClass);
    }
}
