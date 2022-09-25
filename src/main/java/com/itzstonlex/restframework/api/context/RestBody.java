package com.itzstonlex.restframework.api.context;

import com.itzstonlex.restframework.util.RestUtilities;
import lombok.*;

import java.nio.charset.Charset;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestBody {

    /**
     * Factory for creating a simple REST body
     * based on bytes array.
     *
     * @param bytes - Input bytes array.
     * @return - REST body object.
     */
    public static RestBody fromByteArray(byte[] bytes) {
        return new RestBody(new String(bytes));
    }

    /**
     * Factory for creating a simple REST body
     * based on text.
     *
     * @param text - Input text.
     * @return - REST body object.
     */
    public static RestBody fromString(String text) {
        return new RestBody(text);
    }

    /**
     * Factory for creating a simple REST body
     * based on converted object as JSON text.
     *
     * @param object - Input object.
     * @return - REST body object.
     */
    public static RestBody fromConvertedObject(Object object) {
        return fromString(RestUtilities.JSON_PARSER.parse(object));
    }

    private String message;

    public boolean isNull() {
        return message == null;
    }

    public <T> T convert(@NonNull Class<T> type) {
        try {
            return RestUtilities.JSON_PARSER.convert(message, type);
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
