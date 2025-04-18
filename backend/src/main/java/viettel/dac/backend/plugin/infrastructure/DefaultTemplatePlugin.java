package viettel.dac.backend.plugin.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.plugin.core.TemplatePlugin;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of the template plugin.
 * Used as a fallback when no specific plugin is available for a template type.
 */
@Component
@RequiredArgsConstructor
public class DefaultTemplatePlugin implements TemplatePlugin {

    private final ExecutionStrategy defaultExecutionStrategy;

    @Override
    public PluginDescriptor getDescriptor() {
        return PluginDescriptor.builder()
                .type("DEFAULT")
                .name("Default Template Plugin")
                .description("Generic plugin for handling templates with no specialized handler")
                .version("1.0.0")
                .isDefault(true)
                .build();
    }

    @Override
    public boolean validateTemplate(Map<String, Object> templateData) {
        // Basic validation - ensure we have a name at minimum
        return templateData != null && templateData.containsKey("name");
    }

    @Override
    public TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData) {
        TemplateCreateDto dto = TemplateCreateDto.builder()
                .name((String) templateData.get("name"))
                .description(templateData.containsKey("description")
                        ? (String) templateData.get("description") : null)
                .version(templateData.containsKey("version")
                        ? (String) templateData.get("version") : "1.0.0")
                .templateType(templateData.containsKey("templateType")
                        ? TemplateType.valueOf((String) templateData.get("templateType"))
                        : TemplateType.CUSTOM)
                .properties(templateData.containsKey("properties")
                        ? (Map<String, Object>) templateData.get("properties")
                        : null)
                .build();

        // Set tags if provided
        if (templateData.containsKey("tags")) {
            dto.setTags(new HashSet<>((List<String>) templateData.get("tags")));
        }

        return dto;
    }

    @Override
    public BaseTemplate processTemplate(BaseTemplate template, Map<String, Object> templateData, UUID userId) {
        // No additional processing needed for default plugin
        return template;
    }

    @Override
    public ExecutionStrategy getExecutionStrategy() {
        return defaultExecutionStrategy;
    }
}
