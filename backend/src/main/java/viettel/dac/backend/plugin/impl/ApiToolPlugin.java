package viettel.dac.backend.plugin.impl;

import org.springframework.stereotype.Component;
import viettel.dac.backend.execution.engine.ApiExecutionStrategy;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.plugin.ToolPlugin;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ApiToolPlugin implements ToolPlugin {

    private final ApiToolMapper apiToolMapper;
    private final ApiToolTemplateRepository apiToolTemplateRepository;
    private final ApiExecutionStrategy apiExecutionStrategy;

    public ApiToolPlugin(
            ApiToolMapper apiToolMapper,
            ApiToolTemplateRepository apiToolTemplateRepository,
            ApiExecutionStrategy apiExecutionStrategy) {
        this.apiToolMapper = apiToolMapper;
        this.apiToolTemplateRepository = apiToolTemplateRepository;
        this.apiExecutionStrategy = apiExecutionStrategy;
    }

    @Override
    public String getType() {
        return TemplateType.API.name();
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
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public TemplateCreateDto toTemplateCreateDto(Map<String, Object> templateData) {
        // Convert to API-specific DTO first
        ApiToolCreateDto apiDto = new ApiToolCreateDto();

        // Set common fields
        apiDto.setName((String) templateData.get("name"));
        if (templateData.containsKey("description")) {
            apiDto.setDescription((String) templateData.get("description"));
        }
        if (templateData.containsKey("version")) {
            apiDto.setVersion((String) templateData.get("version"));
        }

        // Set API-specific fields
        apiDto.setEndpoint((String) templateData.get("endpoint"));
        apiDto.setHttpMethod(HttpMethod.valueOf((String) templateData.get("httpMethod")));

        if (templateData.containsKey("headers")) {
            apiDto.setHeaders((Map<String, String>) templateData.get("headers"));
        }
        if (templateData.containsKey("queryParams")) {
            apiDto.setQueryParams((Map<String, Object>) templateData.get("queryParams"));
        }
        if (templateData.containsKey("requestBody")) {
            apiDto.setRequestBody(templateData.get("requestBody"));
        }
        if (templateData.containsKey("contentType")) {
            apiDto.setContentType((String) templateData.get("contentType"));
        }
        if (templateData.containsKey("timeout")) {
            apiDto.setTimeout((Integer) templateData.get("timeout"));
        }
        if (templateData.containsKey("followRedirects")) {
            apiDto.setFollowRedirects((Boolean) templateData.get("followRedirects"));
        }

        // Set tags
        if (templateData.containsKey("tags")) {
            apiDto.setTags(new HashSet<>((List<String>) templateData.get("tags")));
        }

        // Convert to general TemplateCreateDto
        return apiToolMapper.toTemplateCreateDto(apiDto);
    }

    @Override
    public ToolTemplate processTemplate(ToolTemplate template, Map<String, Object> templateData, UUID userId) {
        // Create API-specific template
        ApiToolCreateDto apiDto = createApiToolCreateDto(templateData);
        ApiToolTemplate apiTemplate = apiToolMapper.createApiToolTemplate(template, apiDto);

        // Save and return
        apiToolTemplateRepository.save(apiTemplate);
        return template;
    }

    @Override
    public ExecutionStrategy getExecutionStrategy() {
        return apiExecutionStrategy;
    }

    private ApiToolCreateDto createApiToolCreateDto(Map<String, Object> templateData) {
        ApiToolCreateDto dto = new ApiToolCreateDto();

        // Set necessary fields
        dto.setName((String) templateData.get("name"));
        dto.setEndpoint((String) templateData.get("endpoint"));
        dto.setHttpMethod(HttpMethod.valueOf((String) templateData.get("httpMethod")));

        if (templateData.containsKey("description")) {
            dto.setDescription((String) templateData.get("description"));
        }
        if (templateData.containsKey("headers")) {
            dto.setHeaders((Map<String, String>) templateData.get("headers"));
        }
        if (templateData.containsKey("queryParams")) {
            dto.setQueryParams((Map<String, Object>) templateData.get("queryParams"));
        }
        if (templateData.containsKey("requestBody")) {
            dto.setRequestBody(templateData.get("requestBody"));
        }
        if (templateData.containsKey("contentType")) {
            dto.setContentType((String) templateData.get("contentType"));
        }
        if (templateData.containsKey("timeout")) {
            dto.setTimeout((Integer) templateData.get("timeout"));
        }
        if (templateData.containsKey("followRedirects")) {
            dto.setFollowRedirects((Boolean) templateData.get("followRedirects"));
        }

        return dto;
    }
}