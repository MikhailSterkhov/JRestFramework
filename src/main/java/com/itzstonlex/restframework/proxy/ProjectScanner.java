package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.api.RestService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public final class ProjectScanner {

    @Getter(AccessLevel.PACKAGE)
    private ClassLoader bootstrapLoader;

    public Set<Class<?>> scanPackage(@NonNull String packageName) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage(packageName, bootstrapLoader)
                        .setScanners(Scanners.Resources, Scanners.TypesAnnotated)
        );

        return reflections.getTypesAnnotatedWith(RestService.class);
    }

}
