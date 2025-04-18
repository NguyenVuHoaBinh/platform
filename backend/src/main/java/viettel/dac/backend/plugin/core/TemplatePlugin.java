package viettel.dac.backend.plugin.core;

import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.BaseTemplate;

import java.util.Map;
import java.util.UUID;


public interface TemplatePlugin {

    PluginDescriptor getDescriptor();

    boolean validateTemplate(Map<String, Object> templateData);

    TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData);

    BaseTemplate processTemplate(BaseTemplate template, Map<String, Object> templateData, UUID userId);

    ExecutionStrategy getExecutionStrategy();
}