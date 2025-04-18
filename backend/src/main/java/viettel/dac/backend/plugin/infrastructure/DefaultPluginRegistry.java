package viettel.dac.backend.plugin.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.plugin.core.PluginRegistry;
import viettel.dac.backend.plugin.core.TemplatePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of the PluginRegistry interface.
 * Manages the collection of available plugins in the system.
 */
@Component
@Slf4j
public class DefaultPluginRegistry implements PluginRegistry {

    private final Map<String, TemplatePlugin> pluginsMap = new ConcurrentHashMap<>();
    private TemplatePlugin defaultPlugin;

    @Override
    public void registerPlugin(TemplatePlugin plugin) {
        String type = plugin.getDescriptor().getType();
        pluginsMap.put(type, plugin);

        if (plugin.getDescriptor().isDefault()) {
            defaultPlugin = plugin;
            log.info("Default plugin set to: {}", type);
        }

        log.info("Registered plugin: {}", type);
    }

    @Override
    public Optional<TemplatePlugin> getPlugin(String type) {
        return Optional.ofNullable(pluginsMap.get(type));
    }

    @Override
    public TemplatePlugin getDefaultPlugin() {
        if (defaultPlugin == null) {
            throw new IllegalStateException("No default plugin has been registered");
        }
        return defaultPlugin;
    }

    @Override
    public List<TemplatePlugin> getAllPlugins() {
        return new ArrayList<>(pluginsMap.values());
    }

    @Override
    public List<PluginDescriptor> getAllPluginDescriptors() {
        return pluginsMap.values().stream()
                .map(TemplatePlugin::getDescriptor)
                .collect(Collectors.toList());
    }
}