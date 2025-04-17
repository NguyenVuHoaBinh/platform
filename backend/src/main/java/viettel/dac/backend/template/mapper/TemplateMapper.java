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


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    ToolTemplate toEntity(TemplateCreateDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntityFromDto(TemplateUpdateDto dto, @MappingTarget ToolTemplate template);


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
