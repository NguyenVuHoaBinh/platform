package viettel.dac.backend.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import viettel.dac.backend.template.enums.HttpMethod;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTemplateCreateDto extends BaseTemplateDto {

    @NotBlank(message = "Endpoint is required")
    @Size(max = 1000, message = "Endpoint cannot exceed 1000 characters")
    private String endpoint;

    @NotNull(message = "HTTP method is required")
    private HttpMethod httpMethod;

    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private Object requestBody;
    private String contentType;
    private Integer timeout;
    private Boolean followRedirects = true;
}