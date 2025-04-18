package viettel.dac.backend.plugin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.plugin.core.PluginRegistry;
import viettel.dac.backend.plugin.core.TemplatePlugin;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.BaseTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for interacting with plugins.
 * Provides methods for plugin management and template operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PluginService {

    private final PluginRegistry pluginRegistry;

    public TemplatePlugin getPluginForType(String type) {
        return pluginRegistry.getPlugin(type)
                .orElseGet(pluginRegistry::getDefaultPlugin);
    }

    public boolean validateTemplate(String type, Map<String, Object> templateData) {
        return getPluginForType(type).validateTemplate(templateData);
    }

    public TemplateCreateDto toTemplateCreateDto(String type, Map<String, Object> templateData) {
        return getPluginForType(type).toTemplateCreateDto(templateData);
    }

    public BaseTemplate processTemplate(String type, BaseTemplate template, Map<String, Object> templateData, UUID userId) {
        return getPluginForType(type).processTemplate(template, templateData, userId);
    }

    public ExecutionStrategy getExecutionStrategy(String type) {
        return getPluginForType(type).getExecutionStrategy();
    }

    public List<String> getAllPluginTypes() {
        return pluginRegistry.getAllPluginDescriptors().stream()
                .map(PluginDescriptor::getType)
                .toList();
    }

    public List<PluginDescriptor> getAllPluginDescriptors() {
        return pluginRegistry.getAllPluginDescriptors();
    }
}