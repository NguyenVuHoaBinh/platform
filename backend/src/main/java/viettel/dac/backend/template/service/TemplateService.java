package viettel.dac.backend.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceAlreadyExistsException;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.entity.TemplateTag;
import viettel.dac.backend.template.entity.TemplateVersion;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.enums.TemplateType;
import viettel.dac.backend.template.mapper.TemplateMapper;
import viettel.dac.backend.template.repository.TemplateTagRepository;
import viettel.dac.backend.template.repository.TemplateVersionRepository;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final ToolTemplateRepository toolTemplateRepository;
    private final TemplateTagRepository templateTagRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateMapper templateMapper;

    @Transactional
    public TemplateResponseDto createTemplate(TemplateCreateDto createDto, UUID userId) {
        // Check if a template with the same name already exists
        if (toolTemplateRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new ResourceAlreadyExistsException("Template with name '" + createDto.getName() + "' already exists");
        }

        // Create the template entity
        ToolTemplate template = templateMapper.toEntity(createDto);
        template.setCreatedBy(userId);
        template.setLastModifiedBy(userId);

        // Set default version if not provided
        if (template.getVersion() == null) {
            template.setVersion("1.0.0");
        }

        // Save the template
        ToolTemplate savedTemplate = toolTemplateRepository.save(template);

        // Add tags if provided
        if (createDto.getTags() != null && !createDto.getTags().isEmpty()) {
            createDto.getTags().forEach(savedTemplate::addTag);
            savedTemplate = toolTemplateRepository.save(savedTemplate);
        }

        // Create initial version
        createTemplateVersion(savedTemplate, createDto.getProperties(), userId);

        return templateMapper.toDto(savedTemplate);
    }


    @Transactional
    public TemplateVersion createTemplateVersion(ToolTemplate template, Object content, UUID userId) {
        TemplateVersion version = new TemplateVersion();
        version.setTemplate(template);
        version.setVersion(template.getVersion());
        version.setContent(content);
        version.setCreatedBy(userId);
        version.setCreatedAt(Instant.now());
        return templateVersionRepository.save(version);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponseDto> getAllTemplates(
            String name,
            TemplateType templateType,
            Set<String> tags,
            UUID createdBy,
            Pageable pageable) {

        Page<ToolTemplate> templates;

        if (tags != null && !tags.isEmpty()) {
            // If filtering by tags, we need a special query
            templates = toolTemplateRepository.findByTagNames(
                    new ArrayList<>(tags),
                    tags.size(),
                    pageable);
        } else if (name != null || templateType != null || createdBy != null) {
            // If other filters are provided
            templates = toolTemplateRepository.findByFilters(
                    name,
                    templateType,
                    createdBy,
                    null,
                    pageable);
        } else {
            // No filters, get all active templates
            templates = toolTemplateRepository.findByActiveTrue(pageable);
        }

        return templates.map(templateMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#id")
    public TemplateResponseDto getTemplateById(UUID id) {
        ToolTemplate template = findTemplateById(id);
        return templateMapper.toDto(template);
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateResponseDto updateTemplate(UUID id, TemplateUpdateDto updateDto, UUID userId) {
        ToolTemplate template = findTemplateById(id);

        // Check if new name already exists (if name is being changed)
        if (updateDto.getName() != null
                && !updateDto.getName().equalsIgnoreCase(template.getName())
                && toolTemplateRepository.existsByNameIgnoreCaseAndIdNot(updateDto.getName(), id)) {
            throw new ResourceAlreadyExistsException("Template with name '" + updateDto.getName() + "' already exists");
        }

        // Update the entity properties
        templateMapper.updateEntityFromDto(updateDto, template);
        template.setLastModifiedBy(userId);

        // Update tags if provided
        if (updateDto.getTags() != null) {
            updateTemplateTags(template, updateDto.getTags());
        }

        // Save the updated template
        ToolTemplate updatedTemplate = toolTemplateRepository.save(template);

        // Create a new version if the version number was updated
        if (updateDto.getVersion() != null && !updateDto.getVersion().equals(template.getVersion())) {
            createTemplateVersion(updatedTemplate, updateDto.getProperties() != null
                    ? updateDto.getProperties() : template.getProperties(), userId);
        }

        return templateMapper.toDto(updatedTemplate);
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public void deleteTemplate(UUID id) {
        ToolTemplate template = findTemplateById(id);
        template.setActive(false);
        toolTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponseDto> getTemplateVersions(UUID id) {
        // First check if the template exists
        findTemplateById(id);

        // Get all versions
        List<TemplateVersion> versions = templateVersionRepository.findByTemplateIdOrderByVersionDesc(id);

        // Convert to DTOs
        return versions.stream()
                .map(version -> {
                    TemplateResponseDto dto = new TemplateResponseDto();
                    dto.setId(version.getTemplate().getId());
                    dto.setName(version.getTemplate().getName());
                    dto.setDescription(version.getTemplate().getDescription());
                    dto.setVersion(version.getVersion());
                    dto.setTemplateType(version.getTemplate().getTemplateType());
                    dto.setProperties((Map<String, Object>) version.getContent());
                    dto.setTags(templateMapper.tagsToStringSet(version.getTemplate().getTags()));
                    dto.setActive(version.getTemplate().isActive());
                    dto.setCreatedBy(version.getCreatedBy());
                    dto.setCreatedAt(version.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponseDto getTemplateVersion(UUID id, String version) {
        // First check if the template exists
        findTemplateById(id);

        // Get the specific version
        TemplateVersion templateVersion = templateVersionRepository.findByTemplateIdAndVersion(id, version)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + version + " not found for template with ID: " + id));

        // Convert to DTO
        TemplateResponseDto dto = new TemplateResponseDto();
        dto.setId(templateVersion.getTemplate().getId());
        dto.setName(templateVersion.getTemplate().getName());
        dto.setDescription(templateVersion.getTemplate().getDescription());
        dto.setVersion(templateVersion.getVersion());
        dto.setTemplateType(templateVersion.getTemplate().getTemplateType());
        dto.setProperties((Map<String, Object>) templateVersion.getContent());
        dto.setTags(templateMapper.tagsToStringSet(templateVersion.getTemplate().getTags()));
        dto.setActive(templateVersion.getTemplate().isActive());
        dto.setCreatedBy(templateVersion.getCreatedBy());
        dto.setCreatedAt(templateVersion.getCreatedAt());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return templateTagRepository.findAllDistinctTagNames();
    }

    private ToolTemplate findTemplateById(UUID id) {
        return toolTemplateRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + id));
    }

    private void updateTemplateTags(ToolTemplate template, Set<String> newTags) {
        // Get current tag names
        Set<String> currentTags = template.getTags().stream()
                .map(TemplateTag::getTagName)
                .collect(Collectors.toSet());

        // Remove tags that are no longer in the new set
        template.getTags().removeIf(tag -> !newTags.contains(tag.getTagName()));

        // Add new tags
        newTags.stream()
                .filter(tag -> !currentTags.contains(tag))
                .forEach(template::addTag);
    }
}
