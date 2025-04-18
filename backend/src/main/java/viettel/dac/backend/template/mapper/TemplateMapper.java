package viettel.dac.backend.template.mapper;

import org.mapstruct.*;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.entity.TemplateTag;


import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TemplateMapper {

    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToStringSet")
    public abstract TemplateResponseDto toDto(BaseTemplate template);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    public abstract BaseTemplate toEntity(TemplateCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    public abstract void updateEntityFromDto(TemplateUpdateDto dto, @MappingTarget BaseTemplate template);

    @Named("tagsToStringSet")
    public Set<String> tagsToStringSet(Set<TemplateTag> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(TemplateTag::getTagName)
                .collect(Collectors.toSet());
    }

    public void updateTemplateTags(BaseTemplate template, Set<String> newTags) {
        if (newTags == null) {
            return;
        }

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