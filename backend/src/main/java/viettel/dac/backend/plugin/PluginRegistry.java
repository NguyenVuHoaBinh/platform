package viettel.dac.backend.plugin;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PluginRegistry {

    private final Map<String, ToolPlugin> plugins = new HashMap<>();

    public void register(ToolPlugin plugin) {
        plugins.put(plugin.getType(), plugin);
    }

    public ToolPlugin getPlugin(String type) {
        return plugins.get(type);
    }

    public List<ToolPlugin> getAllPlugins() {
        return List.copyOf(plugins.values());
    }

    public List<String> getAllPluginTypes() {
        return plugins.values().stream()
                .map(ToolPlugin::getType)
                .collect(Collectors.toList());
    }
}
