package viettel.dac.backend.plugin.impl;

import org.springframework.stereotype.Component;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.execution.engine.impl.DefaultExecutionStrategy;
import viettel.dac.backend.plugin.ToolPlugin;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DefaultToolPlugin implements ToolPlugin {

    private final DefaultExecutionStrategy defaultExecutionStrategy;

    public DefaultToolPlugin(DefaultExecutionStrategy defaultExecutionStrategy) {
        this.defaultExecutionStrategy = defaultExecutionStrategy;
    }

    @Override
    public String getType() {
        return "DEFAULT";
    }

    @Override
    public boolean validateTemplate(Map<String, Object> templateData) {
        // Basic validation - ensure we have a name at minimum
        return templateData != null && templateData.containsKey("name");
    }

    @Override
    public TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData) {
        TemplateCreateDto dto = new TemplateCreateDto();

        // Set basic fields
        dto.setName((String) templateData.get("name"));
        if (templateData.containsKey("description")) {
            dto.setDescription((String) templateData.get("description"));
        }
        if (templateData.containsKey("version")) {
            dto.setVersion((String) templateData.get("version"));
        }
        if (templateData.containsKey("templateType")) {
            dto.setTemplateType(TemplateType.valueOf((String) templateData.get("templateType")));
        } else {
            dto.setTemplateType(TemplateType.CUSTOM);
        }

        // Set properties
        if (templateData.containsKey("properties")) {
            dto.setProperties((Map<String, Object>) templateData.get("properties"));
        }

        // Set tags
        if (templateData.containsKey("tags")) {
            dto.setTags(new HashSet<>((List<String>) templateData.get("tags")));
        }

        return dto;
    }

    @Override
    public ToolTemplate processTemplate(ToolTemplate template, Map<String, Object> templateData, UUID userId) {
        // No additional processing needed for default plugin
        return template;
    }

    @Override
    public ExecutionStrategy getExecutionStrategy() {
        return defaultExecutionStrategy;
    }
}