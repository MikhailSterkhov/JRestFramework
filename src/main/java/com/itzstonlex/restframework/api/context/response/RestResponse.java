package com.itzstonlex.restframework.api.context.response;

import com.itzstonlex.restframework.api.context.RestBody;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RestResponse {

    private int statusCode;
    private String statusMessage;

    @NonFinal
    private RestBody body;
}
