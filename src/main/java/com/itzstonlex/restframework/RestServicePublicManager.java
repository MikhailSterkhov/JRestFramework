package com.itzstonlex.restframework;

import lombok.NonNull;

public interface RestServicePublicManager {

    <T> T get(@NonNull Class<T> restClientType);

    void bind(@NonNull Class<?> restServerType, Object... initargs);

}
