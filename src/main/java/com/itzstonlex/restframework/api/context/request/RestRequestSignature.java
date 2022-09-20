package com.itzstonlex.restframework.api.context.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class RestRequestSignature {

    private String method;
    private String uri;

    private int timeout;

    private boolean useSignature;
}
