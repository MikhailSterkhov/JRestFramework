package com.itzstonlex.restframework.proxy;

import com.itzstonlex.restframework.api.RestService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public final class ProjectServicesScanner {

    @Getter(AccessLevel.PACKAGE)
    private ClassLoader bootstrapLoader;

    public Set<Class<?>> findServices(@NonNull String packageName) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage(packageName, bootstrapLoader)
                        .setScanners(Scanners.Resources, Scanners.TypesAnnotated)
        );

        return reflections.getTypesAnnotatedWith(RestService.class)
                .stream()
                .filter(Class::isInterface)
                .collect(Collectors.toSet());
    }

}
