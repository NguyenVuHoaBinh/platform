package viettel.dac.backend.execution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.dto.ApiExecutionResultResponseDto;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.entity.ApiExecutionResult;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.mapper.ApiExecutionResultMapper;
import viettel.dac.backend.execution.repository.ApiExecutionResultRepository;
import viettel.dac.backend.execution.repository.ExecutionResultRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiExecutionService {

    private final ExecutionService executionService;
    private final ApiExecutionResultRepository apiExecutionResultRepository;
    private final ExecutionResultRepository executionResultRepository;
    private final ApiExecutionResultMapper apiExecutionResultMapper;

    @Transactional
    public ApiExecutionResultResponseDto executeApiTemplate(ExecutionRequestDto requestDto, UUID userId) {
        // Use the base execution service to execute the template
        ExecutionResult executionResult = executionResultRepository.findById(
                        executionService.executeTemplate(requestDto, userId).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found"));

        // Initially, return the pending execution
        // The API-specific result will be created by the API execution strategy during processing
        ApiExecutionResult pendingResult = ApiExecutionResult.builder()
                .executionResult(executionResult)
                .successful(false)
                .build();

        return apiExecutionResultMapper.toResponseDto(pendingResult);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "apiExecutions", key = "#executionId")
    public ApiExecutionResultResponseDto getApiExecutionResult(UUID executionId) {
        ApiExecutionResult apiExecutionResult = apiExecutionResultRepository.findByIdWithExecutionResult(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("API Execution not found with ID: " + executionId));

        return apiExecutionResultMapper.toResponseDto(apiExecutionResult);
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResultResponseDto> getApiExecutionResultsByTemplate(UUID templateId, Pageable pageable) {
        Page<ApiExecutionResult> results = apiExecutionResultRepository.findByTemplateId(templateId, pageable);

        List<ApiExecutionResultResponseDto> dtos = results.getContent().stream()
                .map(apiExecutionResultMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResultResponseDto> getApiExecutionResultsBySuccess(boolean successful, Pageable pageable) {
        List<ApiExecutionResult> results = apiExecutionResultRepository.findBySuccessful(successful);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<ApiExecutionResult> pagedList = results.subList(start, end);

        List<ApiExecutionResultResponseDto> dtos = pagedList.stream()
                .map(apiExecutionResultMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResultResponseDto> getApiExecutionResultsByStatusCode(int statusCode, Pageable pageable) {
        List<ApiExecutionResult> results = apiExecutionResultRepository.findByStatusCode(statusCode);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<ApiExecutionResult> pagedList = results.subList(start, end);

        List<ApiExecutionResultResponseDto> dtos = pagedList.stream()
                .map(apiExecutionResultMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResultResponseDto> getApiExecutionResultsByResponseTime(long thresholdMs, Pageable pageable) {
        List<ApiExecutionResult> results = apiExecutionResultRepository.findByResponseTimeMsGreaterThan(thresholdMs);

        // Get a sublist based on pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<ApiExecutionResult> pagedList = results.subList(start, end);

        List<ApiExecutionResultResponseDto> dtos = pagedList.stream()
                .map(apiExecutionResultMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResultResponseDto> getApiExecutionResultsByUserAndTemplate(UUID userId, UUID templateId, Pageable pageable) {
        // Get execution results by user ID and optionally template ID
        Page<ExecutionResult> executions;
        if (templateId != null) {
            executions = executionResultRepository.findByTemplateIdAndUserId(templateId, userId, pageable);
        } else {
            executions = executionResultRepository.findByUserId(userId, pageable);
        }

        // Map to API execution results
        List<ApiExecutionResultResponseDto> dtos = new ArrayList<>();
        for (ExecutionResult execution : executions.getContent()) {
            apiExecutionResultRepository.findByIdWithExecutionResult(execution.getId())
                    .ifPresent(apiExecution -> dtos.add(apiExecutionResultMapper.toResponseDto(apiExecution)));
        }

        return new PageImpl<>(dtos, pageable, executions.getTotalElements());
    }
}