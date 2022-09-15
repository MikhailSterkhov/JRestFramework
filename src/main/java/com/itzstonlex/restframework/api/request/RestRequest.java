package com.itzstonlex.restframework.api.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestRequest {

    private String method;
    private String context;

    private int timeout;

    private boolean useSignature;
}
