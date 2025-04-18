package viettel.dac.backend.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceAlreadyExistsException;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.entity.TemplateVersion;
import viettel.dac.backend.template.enums.TemplateType;
import viettel.dac.backend.template.mapper.TemplateMapper;
import viettel.dac.backend.template.repository.TemplateRepository;
import viettel.dac.backend.template.repository.TemplateTagRepository;
import viettel.dac.backend.template.repository.TemplateVersionRepository;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateTagRepository templateTagRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateMapper templateMapper;

    @Transactional
    public TemplateResponseDto createTemplate(TemplateCreateDto createDto, UUID userId) {
        // Check if a template with the same name already exists
        if (templateRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new ResourceAlreadyExistsException("Template with name '" + createDto.getName() + "' already exists");
        }

        // Create the template entity
        BaseTemplate template = templateMapper.toEntity(createDto);
        template.setCreatedBy(userId);
        template.setLastModifiedBy(userId);

        // Set default version if not provided
        if (template.getVersion() == null) {
            template.setVersion("1.0.0");
        }

        // Save the template
        BaseTemplate savedTemplate = templateRepository.save(template);

        // Add tags if provided
        if (createDto.getTags() != null && !createDto.getTags().isEmpty()) {
            createDto.getTags().forEach(savedTemplate::addTag);
            savedTemplate = templateRepository.save(savedTemplate);
        }

        // Create initial version
        createTemplateVersion(savedTemplate, createDto.getProperties(), userId);

        return templateMapper.toDto(savedTemplate);
    }

    @Transactional
    public TemplateVersion createTemplateVersion(BaseTemplate template, Object content, UUID userId) {
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

        // Build specification for dynamic filtering
        Specification<BaseTemplate> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and(TemplateSpecifications.hasNameLike(name));
        }

        if (templateType != null) {
            spec = spec.and(TemplateSpecifications.hasType(templateType));
        }

        if (createdBy != null) {
            spec = spec.and(TemplateSpecifications.hasCreator(createdBy));
        }

        if (tags != null && !tags.isEmpty()) {
            spec = spec.and(TemplateSpecifications.hasTags(tags, tags.size()));
        }

        // Only active templates
        spec = spec.and(TemplateSpecifications.isActive());

        // Execute query with specification
        Page<BaseTemplate> templates = templateRepository.findAll(spec, pageable);

        return templates.map(templateMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#id")
    public TemplateResponseDto getTemplateById(UUID id) {
        BaseTemplate template = findTemplateById(id);
        return templateMapper.toDto(template);
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public TemplateResponseDto updateTemplate(UUID id, TemplateUpdateDto updateDto, UUID userId) {
        BaseTemplate template = findTemplateById(id);

        // Check if new name already exists (if name is being changed)
        if (updateDto.getName() != null
                && !updateDto.getName().equalsIgnoreCase(template.getName())
                && templateRepository.existsByNameIgnoreCaseAndIdNot(updateDto.getName(), id)) {
            throw new ResourceAlreadyExistsException("Template with name '" + updateDto.getName() + "' already exists");
        }

        // Update the entity properties
        templateMapper.updateEntityFromDto(updateDto, template);
        template.setLastModifiedBy(userId);

        // Update tags if provided
        if (updateDto.getTags() != null) {
            templateMapper.updateTemplateTags(template, updateDto.getTags());
        }

        // Save the updated template
        BaseTemplate updatedTemplate = templateRepository.save(template);

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
        BaseTemplate template = findTemplateById(id);
        template.setActive(false);
        templateRepository.save(template);
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
                    BaseTemplate template = version.getTemplate();
                    TemplateResponseDto dto = templateMapper.toDto(template);
                    dto.setVersion(version.getVersion());
                    dto.setProperties((Map<String, Object>) version.getContent());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateResponseDto getTemplateVersion(UUID id, String version) {
        // First check if the template exists
        BaseTemplate template = findTemplateById(id);

        // Get the specific version
        TemplateVersion templateVersion = templateVersionRepository.findByTemplateIdAndVersion(id, version)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + version + " not found for template with ID: " + id));

        // Convert to DTO
        TemplateResponseDto dto = templateMapper.toDto(template);
        dto.setVersion(templateVersion.getVersion());
        dto.setProperties((Map<String, Object>) templateVersion.getContent());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return templateTagRepository.findAllDistinctTagNames();
    }

    protected BaseTemplate findTemplateById(UUID id) {
        return templateRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + id));
    }
}
