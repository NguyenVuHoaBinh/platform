package viettel.dac.backend.plugin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import viettel.dac.backend.plugin.core.PluginRegistry;
import viettel.dac.backend.plugin.core.TemplatePlugin;

import java.util.Map;

/**
 * Configuration for plugin discovery and registration.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PluginConfig {

    private final ApplicationContext applicationContext;
    private final PluginRegistry pluginRegistry;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Discovering and registering plugins...");

        // Get all implementations of TemplatePlugin
        Map<String, TemplatePlugin> plugins = applicationContext.getBeansOfType(TemplatePlugin.class);

        if (plugins.isEmpty()) {
            log.warn("No plugins found in application context!");
            return;
        }

        // Register all discovered plugins
        plugins.values().forEach(plugin -> {
            pluginRegistry.registerPlugin(plugin);
            log.info("Registered plugin: {} ({})",
                    plugin.getDescriptor().getName(),
                    plugin.getDescriptor().getType());
        });

        log.info("Total plugins registered: {}", plugins.size());
    }
}