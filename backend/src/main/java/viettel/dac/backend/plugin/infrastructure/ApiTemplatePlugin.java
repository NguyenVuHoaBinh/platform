package viettel.dac.backend.plugin.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.plugin.core.TemplatePlugin;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ApiTemplate;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;
import viettel.dac.backend.template.repository.ApiTemplateRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API implementation of the template plugin.
 * Specializes in handling API templates.
 */
@Component
@RequiredArgsConstructor
public class ApiTemplatePlugin implements TemplatePlugin {

    private final ApiTemplateRepository apiTemplateRepository;
    private final ExecutionStrategy apiExecutionStrategy;

    @Override
    public PluginDescriptor getDescriptor() {
        return PluginDescriptor.builder()
                .type("API")
                .name("API Template Plugin")
                .description("Plugin for creating and executing API templates")
                .version("1.0.0")
                .isDefault(false)
                .build();
    }

    @Override
    public boolean validateTemplate(Map<String, Object> templateData) {
        if (templateData == null) {
            return false;
        }

        // Validate required fields
        if (!templateData.containsKey("name") || !templateData.containsKey("endpoint") ||
                !templateData.containsKey("httpMethod")) {
            return false;
        }

        // Validate HTTP method
        try {
            String httpMethodStr = (String) templateData.get("httpMethod");
            HttpMethod.valueOf(httpMethodStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData) {
        TemplateCreateDto dto = TemplateCreateDto.builder()
                .name((String) templateData.get("name"))
                .description(templateData.containsKey("description")
                        ? (String) templateData.get("description") : null)
                .version(templateData.containsKey("version")
                        ? (String) templateData.get("version") : "1.0.0")
                .templateType(TemplateType.API)
                .build();

        // Create properties map with API-specific fields
        Map<String, Object> properties = new HashMap<>();
        properties.put("endpoint", templateData.get("endpoint"));
        properties.put("httpMethod", templateData.get("httpMethod"));

        if (templateData.containsKey("headers")) {
            properties.put("headers", templateData.get("headers"));
        }

        if (templateData.containsKey("queryParams")) {
            properties.put("queryParams", templateData.get("queryParams"));
        }

        if (templateData.containsKey("requestBody")) {
            properties.put("requestBody", templateData.get("requestBody"));
        }

        if (templateData.containsKey("contentType")) {
            properties.put("contentType", templateData.get("contentType"));
        }

        if (templateData.containsKey("timeout")) {
            properties.put("timeout", templateData.get("timeout"));
        }

        if (templateData.containsKey("followRedirects")) {
            properties.put("followRedirects", templateData.get("followRedirects"));
        }

        dto.setProperties(properties);

        // Set tags if provided
        if (templateData.containsKey("tags")) {
            dto.setTags(new HashSet<>((List<String>) templateData.get("tags")));
        }

        return dto;
    }

    @Override
    public BaseTemplate processTemplate(BaseTemplate template, Map<String, Object> templateData, UUID userId) {
        // Create API Template if it doesn't exist
        if (!(template instanceof ApiTemplate)) {
            ApiTemplate apiTemplate = ApiTemplate.builder()
                    .endpoint((String) templateData.get("endpoint"))
                    .httpMethod(HttpMethod.valueOf((String) templateData.get("httpMethod")))
                    .build();

            // Set optional fields
            if (templateData.containsKey("headers")) {
                apiTemplate.setHeaders((Map<String, String>) templateData.get("headers"));
            }

            if (templateData.containsKey("queryParams")) {
                apiTemplate.setQueryParams((Map<String, Object>) templateData.get("queryParams"));
            }

            if (templateData.containsKey("requestBody")) {
                apiTemplate.setRequestBody(templateData.get("requestBody"));
            }

            if (templateData.containsKey("contentType")) {
                apiTemplate.setContentType((String) templateData.get("contentType"));
            }

            if (templateData.containsKey("timeout")) {
                apiTemplate.setTimeout((Integer) templateData.get("timeout"));
            }

            if (templateData.containsKey("followRedirects")) {
                apiTemplate.setFollowRedirects((Boolean) templateData.get("followRedirects"));
            }

            apiTemplate.setId(template.getId());
            apiTemplate.setName(template.getName());
            apiTemplate.setDescription(template.getDescription());
            apiTemplate.setVersion(template.getVersion());
            apiTemplate.setActive(template.isActive());
            apiTemplate.setCreatedBy(template.getCreatedBy());
            apiTemplate.setCreatedAt(template.getCreatedAt());
            apiTemplate.setLastModifiedBy(template.getLastModifiedBy());
            apiTemplate.setLastModifiedAt(template.getLastModifiedAt());

            return apiTemplateRepository.save(apiTemplate);
        }

        return template;
    }

    @Override
    public ExecutionStrategy getExecutionStrategy() {
        return apiExecutionStrategy;
    }
}
