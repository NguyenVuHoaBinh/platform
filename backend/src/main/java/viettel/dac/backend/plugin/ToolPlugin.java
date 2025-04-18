package viettel.dac.backend.plugin;

import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ToolTemplate;

import java.util.Map;
import java.util.UUID;

public interface ToolPlugin {

    String getType();
    boolean validateTemplate(Map<String, Object> templateData);
    TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData);
    ToolTemplate processTemplate(ToolTemplate template, Map<String, Object> templateData, UUID userId);
    ExecutionStrategy getExecutionStrategy();
}