package com.itzstonlex.restframework.api.request;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.*;

import java.nio.charset.Charset;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestRequestMessage {

    public static RestRequestMessage asText(String text) {
        return new RestRequestMessage(text);
    }

    public static RestRequestMessage asJsonObject(Object object) {
        return asText(RestUtilities.GSON.toJson(object));
    }

    @ToString.Include
    private String message;

    public boolean isNull() {
        return message == null;
    }

    public <T> T getMessageAsJsonObject(@NonNull Class<T> type) {
        try {
            return RestUtilities.GSON.fromJson(message, type);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public byte[] getMessageAsByteArray() {
        return message.getBytes();
    }

    public byte[] getMessageAsByteArray(Charset charset) {
        return message.getBytes(charset);
    }
}
