package com.itzstonlex.restframework;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.WeakHashMap;

@FieldDefaults(makeFinal = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class RestFrameworkStorage {

    private Map<Class<?>, Object> foundedServices = new WeakHashMap<>();

    void addFoundedServices(Map<Class<?>, Object> foundedServices) {
        this.foundedServices.putAll(foundedServices);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> restClientType) {
        return (T) foundedServices.get(restClientType);
    }

    public void bind(Class<?> restServerType, Object... initargs) {
        if (!foundedServices.containsKey(restServerType)) {
            throw new IllegalArgumentException(restServerType + " is not marked @RestService");
        }

        RestUtilities.createServerProxy(restServerType, initargs);
    }

}
