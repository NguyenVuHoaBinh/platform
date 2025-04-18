package viettel.dac.backend.execution.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.execution.dto.ApiExecutionResponseDto;
import viettel.dac.backend.execution.entity.ApiExecution;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ExecutionMapper.class})
public abstract class ApiExecutionMapper {

    @Autowired
    protected ExecutionMapper executionMapper;

    @Mapping(target = "templateName", expression = "java(executionMapper.getTemplateName(execution.getTemplateId()))")
    @Mapping(target = "durationMs", expression = "java(execution.getDurationMs())")
    public abstract ApiExecutionResponseDto toDto(ApiExecution execution);
}