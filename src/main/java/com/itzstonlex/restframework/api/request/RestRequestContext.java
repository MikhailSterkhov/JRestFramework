package com.itzstonlex.restframework.api.request;

import com.itzstonlex.restframework.api.RestBody;
import com.sun.net.httpserver.Headers;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RestRequestContext {

    public static RestRequestContext create(String method, String resourceName) {
        return new RestRequestContext(method, resourceName);
    }

    private String method, resourceName;

    private Headers headers = new Headers();

    @NonFinal
    private RestBody body;

    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    public String getFirstHeader(String name) {
        return headers.getFirst(name);
    }

    public void setHeader(String name, String value) {
        headers.set(name, value);
    }

    public void addHeader(String name, String value) {
        headers.add(name, value);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void removeHeader(String name, String value) {
        headers.remove(name, value);
    }

    public void removeAllHeaders(String name) {
        headers.remove(name);
    }
}
