package viettel.dac.backend.execution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.dto.ExecutionResponseDto;
import viettel.dac.backend.execution.dto.ExecutionSearchFilterDto;
import viettel.dac.backend.execution.engine.ExecutionEngine;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.enums.ExecutionStatus;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.execution.mapper.ExecutionMapper;
import viettel.dac.backend.execution.repository.ExecutionRepository;
import viettel.dac.backend.execution.service.specification.ExecutionSpecifications;
import viettel.dac.backend.template.repository.TemplateRepository;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final TemplateRepository templateRepository;
    private final ExecutionRepository executionRepository;
    private final ExecutionMapper executionMapper;
    private final ExecutionEngine executionEngine;

    @Value("${execution.default-timeout:60000}")
    private int defaultTimeoutMs;

    @Transactional
    public ExecutionResponseDto executeTemplate(ExecutionRequestDto requestDto, UUID userId) {
        // Check if the template exists
        UUID templateId = requestDto.getTemplateId();
        if (!templateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("Template not found with ID: " + templateId);
        }

        // Create an execution record
        BaseExecution execution = new BaseExecution();
        execution.setTemplateId(templateId);
        execution.setUserId(userId);
        execution.setStatus(ExecutionStatus.PENDING);

        // Save the execution record
        BaseExecution savedExecution = executionRepository.save(execution);

        // Start the execution asynchronously
        executionEngine.execute(
                savedExecution.getId(),
                templateId,
                requestDto.getParameters());

        // Return the pending execution
        return executionMapper.toDto(savedExecution);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "executions", key = "#executionId")
    public ExecutionResponseDto getExecutionResult(UUID executionId) {
        BaseExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        return executionMapper.toDto(execution);
    }

    @Transactional(readOnly = true)
    public Page<ExecutionResponseDto> searchExecutions(ExecutionSearchFilterDto filter, Pageable pageable) {
        Specification<BaseExecution> spec = buildSpecification(filter);

        Page<BaseExecution> executions = executionRepository.findAll(spec, pageable);

        return executions.map(executionMapper::toDto);
    }

    @Transactional
    @CacheEvict(value = "executions", key = "#executionId")
    public ExecutionResponseDto cancelExecution(UUID executionId) {
        BaseExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Check if the execution can be cancelled
        if (execution.getStatus() != ExecutionStatus.PENDING && execution.getStatus() != ExecutionStatus.RUNNING) {
            throw new ExecutionException("Execution cannot be cancelled. Current status: " + execution.getStatus());
        }

        // Try to cancel the execution
        boolean cancelled = executionEngine.cancelExecution(executionId);

        if (cancelled) {
            // Mark as cancelled
            execution.markAsCancelled();
            BaseExecution updatedExecution = executionRepository.save(execution);
            return executionMapper.toDto(updatedExecution);
        } else {
            // If cancellation failed but execution is still running, mark it as cancelled anyway
            if (execution.getStatus() == ExecutionStatus.RUNNING || execution.getStatus() == ExecutionStatus.PENDING) {
                execution.markAsCancelled();
                BaseExecution updatedExecution = executionRepository.save(execution);
                return executionMapper.toDto(updatedExecution);
            }
            throw new ExecutionException("Failed to cancel execution with ID: " + executionId);
        }
    }

    @Transactional
    @CacheEvict(value = "executions", key = "#executionId")
    public void deleteExecutionResult(UUID executionId) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found with ID: " + executionId);
        }

        executionRepository.deleteById(executionId);
    }

    @Transactional
    public int cleanUpOldExecutions(int retentionDays) {
        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        // Find executions to delete
        var oldExecutions = executionRepository.findByEndTimeBefore(cutoffDate);
        int count = oldExecutions.size();

        if (!oldExecutions.isEmpty()) {
            executionRepository.deleteByEndTimeBefore(cutoffDate);
            log.info("Deleted {} old execution results", count);
        }

        return count;
    }

    protected Specification<BaseExecution> buildSpecification(ExecutionSearchFilterDto filter) {
        Specification<BaseExecution> spec = Specification.where(null);

        if (filter.getTemplateId() != null) {
            spec = spec.and(ExecutionSpecifications.hasTemplateId(filter.getTemplateId()));
        }

        if (filter.getUserId() != null) {
            spec = spec.and(ExecutionSpecifications.hasUserId(filter.getUserId()));
        }

        if (filter.getStatus() != null) {
            spec = spec.and(ExecutionSpecifications.hasStatus(filter.getStatus()));
        }

        if (filter.getStartTimeFrom() != null) {
            spec = spec.and(ExecutionSpecifications.startTimeAfter(filter.getStartTimeFrom()));
        }

        if (filter.getStartTimeTo() != null) {
            spec = spec.and(ExecutionSpecifications.startTimeBefore(filter.getStartTimeTo()));
        }

        if (filter.getEndTimeFrom() != null) {
            spec = spec.and(ExecutionSpecifications.endTimeAfter(filter.getEndTimeFrom()));
        }

        if (filter.getEndTimeTo() != null) {
            spec = spec.and(ExecutionSpecifications.endTimeBefore(filter.getEndTimeTo()));
        }

        return spec;
    }
}
