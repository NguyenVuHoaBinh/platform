package viettel.dac.backend.plugin.core;


import viettel.dac.backend.plugin.PluginDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining operations for a plugin registry.
 * The plugin registry maintains a collection of available plugins.
 */
public interface PluginRegistry {

    void registerPlugin(TemplatePlugin plugin);

    Optional<TemplatePlugin> getPlugin(String type);

    TemplatePlugin getDefaultPlugin();

    List<TemplatePlugin> getAllPlugins();

    List<PluginDescriptor> getAllPluginDescriptors();
}