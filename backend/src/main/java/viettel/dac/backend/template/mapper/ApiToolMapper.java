package viettel.dac.backend.template.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.template.dto.ApiToolCreateDto;
import viettel.dac.backend.template.dto.ApiToolResponseDto;
import viettel.dac.backend.template.dto.ApiToolUpdateDto;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.entity.ApiToolTemplate;
import viettel.dac.backend.template.entity.ToolTemplate;

/**
 * MapStruct mapper for converting between ApiToolTemplate entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {TemplateMapper.class})
public abstract class ApiToolMapper {

    @Autowired
    protected TemplateMapper templateMapper;


    @Mapping(target = "templateType", constant = "API")
    @Mapping(target = "properties", ignore = true)
    public abstract TemplateCreateDto toTemplateCreateDto(ApiToolCreateDto dto);


    @Mapping(target = "id", source = "toolTemplate.id")
    @Mapping(target = "name", source = "toolTemplate.name")
    @Mapping(target = "description", source = "toolTemplate.description")
    @Mapping(target = "version", source = "toolTemplate.version")
    @Mapping(target = "tags", source = "toolTemplate.tags", qualifiedByName = "tagsToStringSet")
    @Mapping(target = "active", source = "toolTemplate.active")
    @Mapping(target = "createdBy", source = "toolTemplate.createdBy")
    @Mapping(target = "createdAt", source = "toolTemplate.createdAt")
    @Mapping(target = "lastModifiedBy", source = "toolTemplate.lastModifiedBy")
    @Mapping(target = "lastModifiedAt", source = "toolTemplate.lastModifiedAt")
    public abstract ApiToolResponseDto toResponseDto(ApiToolTemplate template);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "toolTemplate", ignore = true)
    public abstract void updateApiToolTemplate(ApiToolUpdateDto dto, @MappingTarget ApiToolTemplate template);

    public ApiToolTemplate createApiToolTemplate(ToolTemplate toolTemplate, ApiToolCreateDto dto) {
        if (toolTemplate == null || dto == null) {
            return null;
        }

        return ApiToolTemplate.builder()
                .toolTemplate(toolTemplate)
                .endpoint(dto.getEndpoint())
                .httpMethod(dto.getHttpMethod())
                .headers(dto.getHeaders())
                .queryParams(dto.getQueryParams())
                .requestBody(dto.getRequestBody())
                .contentType(dto.getContentType())
                .timeout(dto.getTimeout())
                .followRedirects(dto.getFollowRedirects())
                .build();
    }
}