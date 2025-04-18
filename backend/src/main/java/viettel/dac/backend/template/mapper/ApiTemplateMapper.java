package viettel.dac.backend.template.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.template.dto.ApiTemplateCreateDto;
import viettel.dac.backend.template.dto.ApiTemplateResponseDto;
import viettel.dac.backend.template.dto.ApiTemplateUpdateDto;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ApiTemplate;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {TemplateMapper.class})
public abstract class ApiTemplateMapper {

    @Autowired
    protected TemplateMapper templateMapper;

    @Mapping(target = "templateType", constant = "API")
    @Mapping(target = "properties", ignore = true)
    public abstract TemplateCreateDto toTemplateCreateDto(ApiTemplateCreateDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToStringSet")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastModifiedBy", source = "lastModifiedBy")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt")
    public abstract ApiTemplateResponseDto toResponseDto(ApiTemplate template);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateType", constant = "API")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "active", constant = "true")
    public abstract ApiTemplate createApiTemplate(ApiTemplateCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "properties", ignore = true)
    public abstract void updateApiTemplate(ApiTemplateUpdateDto dto, @MappingTarget ApiTemplate template);
}