package com.itzstonlex.restframework.api.context.request;

import com.itzstonlex.restframework.api.context.RestBody;
import com.sun.net.httpserver.Headers;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.List;

@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RestRequestContext {

    public static RestRequestContext create(String method, String uri) {
        return new RestRequestContext(method, uri);
    }

    @Getter
    private String method, uri;

    @NonFinal
    @Getter
    private RestBody body;

    @Getter
    private Headers headers = new Headers();

    /**
     * Getting all cached HTTP request headers
     * by input name.
     *
     * @param name - Headers name.
     * @return - List of header values.
     */
    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    /**
     * Getting a first cached HTTP request header
     * by input name.
     *
     * @param name - Header name.
     * @return - First value by header name.
     */
    public String getFirstHeader(String name) {
        return headers.getFirst(name);
    }

    /**
     * Set single header value by input name.
     *
     * @param name - Header name.
     * @param value - Header single value.
     */
    public void setHeader(String name, String value) {
        headers.set(name, value);
    }

    /**
     * Put new header value by input name.
     *
     * @param name - Header name.
     * @param value - Header value.
     */
    public void addHeader(String name, String value) {
        headers.add(name, value);
    }

    /**
     * Delete cached header value by input name.
     *
     * @param name - Header name.
     * @param value - Header value.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public void removeHeader(String name, String value) {
        headers.remove(name, value);
    }

    /**
     * Delete all cached headers by input name.
     *
     * @param name - Header name.
     */
    public void removeAllHeaders(String name) {
        headers.remove(name);
    }
}
