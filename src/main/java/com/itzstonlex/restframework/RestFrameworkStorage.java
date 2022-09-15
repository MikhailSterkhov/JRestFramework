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

    private Map<Class<?>, Object> restClientsMap = new WeakHashMap<>();

    void addRestClients(Map<Class<?>, Object> servicesMap) {
        this.restClientsMap.putAll(servicesMap);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> restClientType) {
        return (T) restClientsMap.get(restClientType);
    }

    public <T> T initServer(Class<T> restServerType, Object... initargs) {
        return RestUtilities.createServerProxy(restServerType, initargs);
    }
}
