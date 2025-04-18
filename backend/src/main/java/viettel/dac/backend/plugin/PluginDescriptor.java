package viettel.dac.backend.plugin;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class PluginDescriptor {
    private final String type;
    private final String name;
    private final String description;
    private final String version;
    private final boolean isDefault;
}
