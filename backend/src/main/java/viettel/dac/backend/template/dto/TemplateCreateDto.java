package viettel.dac.backend.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateDto extends BaseTemplateDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Template type is required")
    private TemplateType templateType;

    private Map<String, Object> properties;
}
