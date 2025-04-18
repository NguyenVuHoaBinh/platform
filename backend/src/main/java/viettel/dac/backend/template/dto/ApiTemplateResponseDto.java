package viettel.dac.backend.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;


import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTemplateResponseDto extends BaseTemplateDto {

    private UUID id;
    private boolean active;
    private UUID createdBy;
    private Instant createdAt;
    private UUID lastModifiedBy;
    private Instant lastModifiedAt;

    // API-specific fields
    private String endpoint;
    private HttpMethod httpMethod;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private Object requestBody;
    private String contentType;
    private Integer timeout;
    private Boolean followRedirects;

    // Automatically set API type
    private final TemplateType templateType = TemplateType.API;
}
