package viettel.dac.backend.execution.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.execution.dto.ExecutionResponseDto;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.template.repository.TemplateRepository;


import java.util.UUID;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ExecutionMapper {

    @Autowired
    protected TemplateRepository templateRepository;

    @Mapping(target = "templateName", expression = "java(getTemplateName(execution.getTemplateId()))")
    @Mapping(target = "durationMs", expression = "java(execution.getDurationMs())")
    public abstract ExecutionResponseDto toDto(BaseExecution execution);

    protected String getTemplateName(UUID templateId) {
        return templateRepository.findById(templateId)
                .map(template -> template.getName())
                .orElse(null);
    }
}
