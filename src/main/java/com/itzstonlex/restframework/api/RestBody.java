package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.*;

import java.nio.charset.Charset;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestBody {

    public static RestBody asText(String text) {
        return new RestBody(text);
    }

    public static RestBody asJsonObject(Object object) {
        return asText(RestUtilities.GSON.toJson(object));
    }

    @ToString.Include
    private String value;

    public boolean isNull() {
        return value == null;
    }

    public <T> T getBodyAsJsonObject(@NonNull Class<T> type) {
        try {
            return RestUtilities.GSON.fromJson(value, type);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public byte[] getBodyAsByteArray() {
        return value.getBytes();
    }

    public byte[] getBodyAsByteArray(Charset charset) {
        return value.getBytes(charset);
    }
}
