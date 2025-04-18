package viettel.dac.backend.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import viettel.dac.backend.plugin.PluginRegistry;
import viettel.dac.backend.plugin.ToolPlugin;

import java.util.Map;

@Configuration
public class PluginConfig {

    private final ApplicationContext applicationContext;
    private final PluginRegistry pluginRegistry;

    public PluginConfig(ApplicationContext applicationContext, PluginRegistry pluginRegistry) {
        this.applicationContext = applicationContext;
        this.pluginRegistry = pluginRegistry;
    }

    /**
     * Listen for context refreshed event to discover and register all plugins
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Discover all ToolPlugin implementations
        Map<String, ToolPlugin> plugins = applicationContext.getBeansOfType(ToolPlugin.class);

        // Register all discovered plugins
        plugins.values().forEach(pluginRegistry::register);
    }
}