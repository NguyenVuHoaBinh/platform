package viettel.dac.backend.execution.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.execution.dto.ApiExecutionResultResponseDto;
import viettel.dac.backend.execution.entity.ApiExecutionResult;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ApiExecutionResultMapper {

    @Autowired
    protected ToolTemplateRepository toolTemplateRepository;

    @Mapping(target = "id", source = "executionResult.id")
    @Mapping(target = "templateId", source = "executionResult.templateId")
    @Mapping(target = "templateName", expression = "java(getTemplateName(apiExecutionResult.getExecutionResult().getTemplateId()))")
    @Mapping(target = "userId", source = "executionResult.userId")
    @Mapping(target = "status", source = "executionResult.status")
    @Mapping(target = "startTime", source = "executionResult.startTime")
    @Mapping(target = "endTime", source = "executionResult.endTime")
    @Mapping(target = "durationMs", expression = "java(apiExecutionResult.getExecutionResult().getDurationMs())")
    @Mapping(target = "errorMessage", source = "executionResult.errorMessage")
    @Mapping(target = "metrics", source = "executionResult.metrics")
    public abstract ApiExecutionResultResponseDto toResponseDto(ApiExecutionResult apiExecutionResult);

    /**
     * Creates a new ApiExecutionResult entity
     */
    public ApiExecutionResult createApiExecutionResult(
            ExecutionResult executionResult,
            Integer statusCode,
            Map<String, String> responseHeaders,
            Object responseBody,
            Long responseTimeMs,
            Boolean successful) {

        ApiExecutionResult result = new ApiExecutionResult();
        result.setExecutionResult(executionResult);
        result.setStatusCode(statusCode);
        result.setResponseHeaders(responseHeaders);
        result.setResponseBody(responseBody);
        result.setResponseTimeMs(responseTimeMs);
        result.setSuccessful(successful);
        return result;
    }

    protected String getTemplateName(UUID templateId) {
        return toolTemplateRepository.findById(templateId)
                .map(template -> template.getName())
                .orElse(null);
    }
}