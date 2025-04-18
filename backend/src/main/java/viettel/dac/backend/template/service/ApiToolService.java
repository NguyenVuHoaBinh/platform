package viettel.dac.backend.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.template.dto.*;
import viettel.dac.backend.template.entity.ApiToolTemplate;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.mapper.ApiToolMapper;
import viettel.dac.backend.template.repository.ApiToolTemplateRepository;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApiToolService {

    private final ApiToolTemplateRepository apiToolTemplateRepository;
    private final ToolTemplateRepository toolTemplateRepository;
    private final TemplateService templateService;
    private final ApiToolMapper apiToolMapper;

    @Transactional
    public ApiToolResponseDto createApiTemplate(ApiToolCreateDto createDto, UUID userId) {
        // Convert to base template DTO
        TemplateCreateDto templateCreateDto = apiToolMapper.toTemplateCreateDto(createDto);

        // Create the base template
        ToolTemplate toolTemplate = toolTemplateRepository.findById(
                        templateService.createTemplate(templateCreateDto, userId).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to create base template"));

        // Create the API template
        ApiToolTemplate apiToolTemplate = apiToolMapper.createApiToolTemplate(toolTemplate, createDto);
        apiToolTemplate = apiToolTemplateRepository.save(apiToolTemplate);

        return apiToolMapper.toResponseDto(apiToolTemplate);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "apiTemplates", key = "#id")
    public ApiToolResponseDto getApiTemplateById(UUID id) {
        ApiToolTemplate apiToolTemplate = apiToolTemplateRepository.findByIdWithToolTemplate(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));

        return apiToolMapper.toResponseDto(apiToolTemplate);
    }

    @Transactional
    @CacheEvict(value = "apiTemplates", key = "#id")
    public ApiToolResponseDto updateApiTemplate(UUID id, ApiToolUpdateDto updateDto, UUID userId) {
        // Get the API template with its tool template
        ApiToolTemplate apiToolTemplate = apiToolTemplateRepository.findByIdWithToolTemplate(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));

        // Update the base template if needed
        if (updateDto.getName() != null || updateDto.getDescription() != null ||
                updateDto.getVersion() != null || updateDto.getTags() != null ||
                updateDto.getActive() != null) {

            // Use the template service to update the base template
            templateService.updateTemplate(id,
                    buildBaseTemplateUpdateDto(updateDto), userId);

            // Refresh the entity after the update
            apiToolTemplate = apiToolTemplateRepository.findByIdWithToolTemplate(id)
                    .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));
        }

        // Update the API-specific fields
        apiToolMapper.updateApiToolTemplate(updateDto, apiToolTemplate);

        // Save the updated API template
        apiToolTemplate = apiToolTemplateRepository.save(apiToolTemplate);

        return apiToolMapper.toResponseDto(apiToolTemplate);
    }

    @Transactional(readOnly = true)
    public Page<ApiToolResponseDto> getApiTemplatesByHttpMethod(HttpMethod httpMethod, Pageable pageable) {
        List<ApiToolTemplate> templates = apiToolTemplateRepository.findByHttpMethod(httpMethod);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), templates.size());
        List<ApiToolTemplate> pagedList = templates.subList(start, end);

        // Map to DTOs
        List<ApiToolResponseDto> dtos = pagedList.stream()
                .map(apiToolMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, templates.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiToolResponseDto> getApiTemplatesByDomain(String domain, Pageable pageable) {
        List<ApiToolTemplate> templates = apiToolTemplateRepository.findByDomain(domain);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), templates.size());
        List<ApiToolTemplate> pagedList = templates.subList(start, end);

        // Map to DTOs
        List<ApiToolResponseDto> dtos = pagedList.stream()
                .map(apiToolMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, templates.size());
    }

    private viettel.dac.backend.template.dto.TemplateUpdateDto buildBaseTemplateUpdateDto(
            ApiToolUpdateDto apiUpdateDto) {
        return viettel.dac.backend.template.dto.TemplateUpdateDto.builder()
                .name(apiUpdateDto.getName())
                .description(apiUpdateDto.getDescription())
                .version(apiUpdateDto.getVersion())
                .tags(apiUpdateDto.getTags())
                .active(apiUpdateDto.getActive())
                .build();
    }
}