package viettel.dac.backend.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import viettel.dac.backend.template.enums.TemplateType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponseDto extends BaseTemplateDto {

    private UUID id;
    private TemplateType templateType;
    private Map<String, Object> properties;
    private boolean active;
    private UUID createdBy;
    private Instant createdAt;
    private UUID lastModifiedBy;
    private Instant lastModifiedAt;
}