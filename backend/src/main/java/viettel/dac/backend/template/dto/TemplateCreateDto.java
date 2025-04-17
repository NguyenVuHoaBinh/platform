package viettel.dac.backend.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Template type is required")
    private TemplateType templateType;

    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in format x.y.z")
    private String version;

    private Map<String, Object> properties;

    private Set<String> tags = new HashSet<>();
}

