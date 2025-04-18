package viettel.dac.backend.template.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import viettel.dac.backend.template.enums.HttpMethod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiToolCreateDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in format x.y.z")
    private String version;

    private Set<String> tags = new HashSet<>();

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