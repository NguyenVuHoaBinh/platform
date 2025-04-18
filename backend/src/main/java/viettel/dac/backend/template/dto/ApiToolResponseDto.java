package viettel.dac.backend.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiToolResponseDto {

    private UUID id;
    private String name;
    private String description;
    private String version;
    private TemplateType templateType = TemplateType.API;
    private Set<String> tags = new HashSet<>();
    private boolean active;
    private UUID createdBy;
    private Instant createdAt;
    private UUID lastModifiedBy;
    private Instant lastModifiedAt;

    private String endpoint;
    private HttpMethod httpMethod;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private Object requestBody;
    private String contentType;
    private Integer timeout;
    private Boolean followRedirects;
}
