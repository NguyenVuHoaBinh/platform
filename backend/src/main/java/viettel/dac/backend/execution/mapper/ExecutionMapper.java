package viettel.dac.backend.execution.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.execution.dto.ExecutionResultResponseDto;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.util.UUID;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ExecutionMapper {

    @Autowired
    protected ToolTemplateRepository toolTemplateRepository;

    @Mapping(target = "templateId", source = "templateId")
    @Mapping(target = "templateName", expression = "java(getTemplateName(executionResult.getTemplateId()))")
    @Mapping(target = "durationMs", expression = "java(executionResult.getDurationMs())")
    public abstract ExecutionResultResponseDto toDto(ExecutionResult executionResult);

    @Named("getTemplateName")
    protected String getTemplateName(UUID templateId) {
        return toolTemplateRepository.findById(templateId)
                .map(template -> template.getName())
                .orElse(null);
    }
}