package viettel.dac.backend.plugin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ValidationException;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.repository.TemplateRepository;
import viettel.dac.backend.template.service.TemplateService;

import java.util.Map;
import java.util.UUID;

/**
 * Service for creating templates through the plugin system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PluginTemplateService {

    private final PluginService pluginService;
    private final TemplateService templateService;
    private final TemplateRepository templateRepository;

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
        BaseTemplate template = templateRepository.findById(responseDto.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created template"));

        // Process the template with the plugin
        pluginService.processTemplate(type, template, templateData, userId);

        // Return the response DTO
        return responseDto;
    }
}