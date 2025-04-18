package viettel.dac.backend.template.mapper;

import org.mapstruct.*;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.entity.TemplateTag;
import viettel.dac.backend.template.entity.ToolTemplate;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToStringSet")
    TemplateResponseDto toDto(ToolTemplate template);

    /**
     * Convert DTO to entity.
     * Note that we use a default implementation rather than letting MapStruct generate
     * this to avoid issues with the inherited fields from BaseEntity.
     */
    default ToolTemplate toEntity(TemplateCreateDto dto) {
        if (dto == null) {
            return null;
        }

        ToolTemplate template = new ToolTemplate();
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        template.setTemplateType(dto.getTemplateType());
        template.setProperties(dto.getProperties());
        template.setActive(true);

        return template;
    }

    /**
     * Update entity from DTO
     */
    default void updateEntityFromDto(TemplateUpdateDto dto, @MappingTarget ToolTemplate template) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            template.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            template.setDescription(dto.getDescription());
        }
        if (dto.getVersion() != null) {
            template.setVersion(dto.getVersion());
        }
        if (dto.getProperties() != null) {
            template.setProperties(dto.getProperties());
        }
        if (dto.getActive() != null) {
            template.setActive(dto.getActive());
        }
    }

    @Named("tagsToStringSet")
    default Set<String> tagsToStringSet(Set<TemplateTag> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(TemplateTag::getTagName)
                .collect(Collectors.toSet());
    }
}