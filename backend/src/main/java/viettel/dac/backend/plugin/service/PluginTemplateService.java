package viettel.dac.backend.plugin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ValidationException;
import viettel.dac.backend.plugin.PluginService;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.repository.ToolTemplateRepository;
import viettel.dac.backend.template.service.TemplateService;

import java.util.Map;
import java.util.UUID;

@Service
public class PluginTemplateService {

    private final PluginService pluginService;
    private final TemplateService templateService;
    private final ToolTemplateRepository toolTemplateRepository;

    public PluginTemplateService(
            PluginService pluginService,
            TemplateService templateService,
            ToolTemplateRepository toolTemplateRepository) {
        this.pluginService = pluginService;
        this.templateService = templateService;
        this.toolTemplateRepository = toolTemplateRepository;
    }

    /**
     * Create a template using the plugin system
     */
    @Transactional
    public TemplateResponseDto createTemplate(String type, Map<String, Object> templateData, UUID userId) {
        // Validate the template data
        if (!pluginService.validateTemplate(type, templateData)) {
            throw new ValidationException("Invalid template data for type: " + type);
        }

        // Convert to TemplateCreateDto
        TemplateCreateDto createDto = pluginService.toTemplateCreateDto(type, templateData);

        // Create the base template
        TemplateResponseDto responseDto = templateService.createTemplate(createDto, userId);

        // Get the created template
        ToolTemplate template = toolTemplateRepository.findById(responseDto.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created template"));

        // Process the template with the plugin
        ToolTemplate processedTemplate = pluginService.processTemplate(type, template, templateData, userId);

        // Return the response DTO
        return responseDto;
    }
}