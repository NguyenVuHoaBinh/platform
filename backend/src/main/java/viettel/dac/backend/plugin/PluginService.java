package viettel.dac.backend.plugin;

import org.springframework.stereotype.Service;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ToolTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PluginService {

    private final PluginRegistry registry;
    private final ToolPlugin defaultPlugin;

    public PluginService(PluginRegistry registry, List<ToolPlugin> plugins) {
        this.registry = registry;

        // Register all plugins
        ToolPlugin foundDefault = null;
        for (ToolPlugin plugin : plugins) {
            registry.register(plugin);
            if ("DEFAULT".equals(plugin.getType())) {
                foundDefault = plugin;
            }
        }

        this.defaultPlugin = foundDefault;
        if (this.defaultPlugin == null) {
            throw new IllegalStateException("No default plugin found");
        }
    }

    public ToolPlugin getPluginForType(String type) {
        ToolPlugin plugin = registry.getPlugin(type);
        return plugin != null ? plugin : defaultPlugin;
    }

    public boolean validateTemplate(String type, Map<String, Object> templateData) {
        return getPluginForType(type).validateTemplate(templateData);
    }

    public TemplateCreateDto toTemplateCreateDto(String type, Map<String, Object> templateData) {
        return getPluginForType(type).toTemplateCreateDto(templateData);
    }

    public ToolTemplate processTemplate(String type, ToolTemplate template, Map<String, Object> templateData, UUID userId) {
        return getPluginForType(type).processTemplate(template, templateData, userId);
    }

    public ExecutionStrategy getExecutionStrategy(String type) {
        return getPluginForType(type).getExecutionStrategy();
    }

    public List<String> getAllPluginTypes() {
        return registry.getAllPluginTypes();
    }
}