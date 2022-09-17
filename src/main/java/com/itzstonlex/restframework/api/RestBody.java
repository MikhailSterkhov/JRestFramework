package com.itzstonlex.restframework.api;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.*;

import java.nio.charset.Charset;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestBody {

    public static RestBody asText(String text) {
        return new RestBody(text);
    }

    public static RestBody asJsonObject(Object object) {
        return asText(RestUtilities.GSON.toJson(object));
    }

    private String message;

    public boolean isNull() {
        return message == null;
    }

    public <T> T getAsJsonObject(@NonNull Class<T> type) {
        try {
            return RestUtilities.GSON.fromJson(message, type);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public byte[] getAsByteArray() {
        return message.getBytes();
    }

    public byte[] getAsByteArray(Charset charset) {
        return message.getBytes(charset);
    }

    @Override
    public String toString() {
        return message;
    }

}
