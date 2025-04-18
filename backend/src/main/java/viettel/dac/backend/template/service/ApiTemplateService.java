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
import viettel.dac.backend.template.dto.ApiTemplateCreateDto;
import viettel.dac.backend.template.dto.ApiTemplateResponseDto;
import viettel.dac.backend.template.dto.ApiTemplateUpdateDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.entity.ApiTemplate;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.mapper.ApiTemplateMapper;
import viettel.dac.backend.template.mapper.TemplateMapper;
import viettel.dac.backend.template.repository.ApiTemplateRepository;
import viettel.dac.backend.template.repository.TemplateRepository;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiTemplateService {

    private final ApiTemplateRepository apiTemplateRepository;
    private final TemplateRepository templateRepository;
    private final TemplateService templateService;
    private final ApiTemplateMapper apiTemplateMapper;
    private final TemplateMapper templateMapper;

    @Transactional
    public ApiTemplateResponseDto createApiTemplate(ApiTemplateCreateDto createDto, UUID userId) {
        // Create new API template directly
        ApiTemplate apiTemplate = apiTemplateMapper.createApiTemplate(createDto);

        // Set audit fields
        apiTemplate.setCreatedBy(userId);
        apiTemplate.setLastModifiedBy(userId);

        // Set version if not provided
        if (apiTemplate.getVersion() == null) {
            apiTemplate.setVersion("1.0.0");
        }

        // Add tags if provided
        if (createDto.getTags() != null && !createDto.getTags().isEmpty()) {
            createDto.getTags().forEach(apiTemplate::addTag);
        }

        // Save the template
        ApiTemplate savedTemplate = apiTemplateRepository.save(apiTemplate);

        // Create initial version
        templateService.createTemplateVersion(savedTemplate, savedTemplate.getProperties(), userId);

        return apiTemplateMapper.toResponseDto(savedTemplate);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "apiTemplates", key = "#id")
    public ApiTemplateResponseDto getApiTemplateById(UUID id) {
        ApiTemplate apiTemplate = apiTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));

        return apiTemplateMapper.toResponseDto(apiTemplate);
    }

    @Transactional
    @CacheEvict(value = "apiTemplates", key = "#id")
    public ApiTemplateResponseDto updateApiTemplate(UUID id, ApiTemplateUpdateDto updateDto, UUID userId) {
        // Get the template
        ApiTemplate apiTemplate = apiTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));

        // Update base template fields if needed
        if (updateDto.getName() != null || updateDto.getDescription() != null ||
                updateDto.getVersion() != null || updateDto.getTags() != null ||
                updateDto.getActive() != null) {

            // Create a TemplateUpdateDto from ApiTemplateUpdateDto
            TemplateUpdateDto baseUpdateDto = TemplateUpdateDto.builder()
                    .name(updateDto.getName())
                    .description(updateDto.getDescription())
                    .version(updateDto.getVersion())
                    .tags(updateDto.getTags())
                    .active(updateDto.getActive())
                    .build();

            // Update base fields using the template service
            templateService.updateTemplate(id, baseUpdateDto, userId);

            // Refresh the entity after the update
            apiTemplate = apiTemplateRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("API Template not found with ID: " + id));
        }

        // Update API-specific fields
        apiTemplateMapper.updateApiTemplate(updateDto, apiTemplate);
        apiTemplate.setLastModifiedBy(userId);

        // Save the updated API template
        apiTemplate = apiTemplateRepository.save(apiTemplate);

        // Create a new version if version was updated
        if (updateDto.getVersion() != null && !updateDto.getVersion().equals(apiTemplate.getVersion())) {
            templateService.createTemplateVersion(apiTemplate, apiTemplate.getProperties(), userId);
        }

        return apiTemplateMapper.toResponseDto(apiTemplate);
    }

    @Transactional(readOnly = true)
    public Page<ApiTemplateResponseDto> getApiTemplatesByHttpMethod(HttpMethod httpMethod, Pageable pageable) {
        List<ApiTemplate> templates = apiTemplateRepository.findByHttpMethod(httpMethod);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), templates.size());
        List<ApiTemplate> pagedList = templates.subList(start, end);

        // Map to DTOs
        List<ApiTemplateResponseDto> dtos = pagedList.stream()
                .map(apiTemplateMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, templates.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiTemplateResponseDto> getApiTemplatesByDomain(String domain, Pageable pageable) {
        List<ApiTemplate> templates = apiTemplateRepository.findByDomain(domain);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), templates.size());
        List<ApiTemplate> pagedList = templates.subList(start, end);

        // Map to DTOs
        List<ApiTemplateResponseDto> dtos = pagedList.stream()
                .map(apiTemplateMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, templates.size());
    }
}
