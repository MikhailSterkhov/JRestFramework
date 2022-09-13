package com.itzstonlex.restframework.api;

import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RestRequestMessage {

    private static final Gson GSON = new Gson();

    public static RestRequestMessage asText(String text) {
        return new RestRequestMessage(text);
    }

    public static RestRequestMessage asJsonObject(Object object) {
        return asText(GSON.toJson(object));
    }

    @SuppressWarnings("resource")
    public static RestRequestMessage asXML(File file) {
        if (!file.getName().endsWith("xml")) {
            throw new IllegalArgumentException("file " + file + " type is not a XML");
        }

        try {
            return asText(Files.lines(file.toPath()).collect(Collectors.joining("\n")));
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @ToString.Include
    private final String message;

    public boolean isNull() {
        return message == null;
    }
}
