package com.itzstonlex.restframework;

import com.itzstonlex.restframework.proxy.ProjectServicesScanner;
import com.itzstonlex.restframework.proxy.ServiceProxyManager;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.Map;

@Getter
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestFrameworkBootstrap {

    public static RestFrameworkStorage runServices(@NonNull String packageName, @NonNull ClassLoader classLoader) {
        RestFrameworkBootstrap restFramework = new RestFrameworkBootstrap(
                new ServiceProxyManager(new ProjectServicesScanner(classLoader))
        );

        restFramework.initProjectServices(packageName);
        return restFramework.getStorage();
    }

    public static RestFrameworkStorage runServices(@NonNull Class<?> bootstrapClass) {
        return runServices(bootstrapClass.getPackage().getName(), bootstrapClass.getClassLoader());
    }

    private Object lock = new Object();
    private ServiceProxyManager serviceProxyManager;

    @NonFinal
    private RestFrameworkStorage storage;

    public void initProjectServices(@NonNull String packageName) {
        synchronized (lock) {

            storage = new RestFrameworkStorage();
            storage.addServices(
                    serviceProxyManager.findServices(packageName)
            );
        }
    }

}
