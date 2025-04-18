package viettel.dac.backend.execution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.dto.ApiExecutionResponseDto;
import viettel.dac.backend.execution.dto.ApiExecutionSearchFilterDto;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.entity.ApiExecution;
import viettel.dac.backend.execution.mapper.ApiExecutionMapper;
import viettel.dac.backend.execution.repository.ApiExecutionRepository;
import viettel.dac.backend.execution.service.specification.ApiExecutionSpecifications;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiExecutionService {

    private final ExecutionService executionService;
    private final ApiExecutionRepository apiExecutionRepository;
    private final ApiExecutionMapper apiExecutionMapper;

    @Transactional
    public ApiExecutionResponseDto executeApiTemplate(ExecutionRequestDto requestDto, UUID userId) {
        // Use the base execution service to start execution
        executionService.executeTemplate(requestDto, userId);

        // Find the execution (it should be in PENDING state now)
        ApiExecution execution = apiExecutionRepository.findById(requestDto.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("API Execution not found"));

        // Return the pending execution information
        return apiExecutionMapper.toDto(execution);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "apiExecutions", key = "#executionId")
    public ApiExecutionResponseDto getApiExecutionResult(UUID executionId) {
        ApiExecution apiExecution = apiExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("API Execution not found with ID: " + executionId));

        return apiExecutionMapper.toDto(apiExecution);
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResponseDto> searchApiExecutions(ApiExecutionSearchFilterDto filter, Pageable pageable) {
        // Build base specification
        Specification<ApiExecution> spec = buildSpecification(filter);

        // Add API-specific filters
        if (filter.getStatusCode() != null) {
            spec = spec.and(ApiExecutionSpecifications.hasStatusCode(filter.getStatusCode()));
        }

        if (filter.getSuccessful() != null) {
            spec = spec.and(ApiExecutionSpecifications.isSuccessful(filter.getSuccessful()));
        }

        if (filter.getMaxResponseTimeMs() != null) {
            spec = spec.and(ApiExecutionSpecifications.responseTimeLessThan(filter.getMaxResponseTimeMs()));
        }

        Page<ApiExecution> executions = apiExecutionRepository.findAll(spec, pageable);

        return executions.map(apiExecutionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResponseDto> getApiExecutionsByStatusCode(int statusCode, Pageable pageable) {
        List<ApiExecution> results = apiExecutionRepository.findByStatusCode(statusCode);

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<ApiExecution> pagedList = results.subList(start, end);

        // Map to DTOs
        List<ApiExecutionResponseDto> dtos = pagedList.stream()
                .map(apiExecutionMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.size());
    }

    @Transactional(readOnly = true)
    public Page<ApiExecutionResponseDto> getApiExecutionsBySuccessful(boolean successful, Pageable pageable) {
        List<ApiExecution> results = apiExecutionRepository.findBySuccessful(successful);

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        List<ApiExecution> pagedList = results.subList(start, end);

        // Map to DTOs
        List<ApiExecutionResponseDto> dtos = pagedList.stream()
                .map(apiExecutionMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.size());
    }

    @Transactional
    @CacheEvict(value = "apiExecutions", key = "#executionId")
    public void deleteApiExecution(UUID executionId) {
        if (!apiExecutionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("API Execution not found with ID: " + executionId);
        }

        apiExecutionRepository.deleteById(executionId);
    }

    protected Specification<ApiExecution> buildSpecification(ApiExecutionSearchFilterDto filter) {
        Specification<ApiExecution> spec = Specification.where(null);

        // Apply base execution filters
        if (filter.getTemplateId() != null) {
            spec = spec.and(ApiExecutionSpecifications.hasTemplateId(filter.getTemplateId()));
        }

        if (filter.getUserId() != null) {
            spec = spec.and(ApiExecutionSpecifications.hasUserId(filter.getUserId()));
        }

        if (filter.getStatus() != null) {
            spec = spec.and(ApiExecutionSpecifications.hasStatus(filter.getStatus()));
        }

        if (filter.getStartTimeFrom() != null) {
            spec = spec.and(ApiExecutionSpecifications.startTimeAfter(filter.getStartTimeFrom()));
        }

        if (filter.getStartTimeTo() != null) {
            spec = spec.and(ApiExecutionSpecifications.startTimeBefore(filter.getStartTimeTo()));
        }

        if (filter.getEndTimeFrom() != null) {
            spec = spec.and(ApiExecutionSpecifications.endTimeAfter(filter.getEndTimeFrom()));
        }

        if (filter.getEndTimeTo() != null) {
            spec = spec.and(ApiExecutionSpecifications.endTimeBefore(filter.getEndTimeTo()));
        }

        return spec;
    }
}
