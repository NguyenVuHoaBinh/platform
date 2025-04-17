package viettel.dac.backend.template.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;


import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUpdateDto {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in format x.y.z")
    private String version;

    private Map<String, Object> properties;

    private Set<String> tags;

    private Boolean active;
}