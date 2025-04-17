package viettel.dac.backend.execution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.dto.ExecutionResultResponseDto;
import viettel.dac.backend.execution.engine.impl.ExecutionEngine;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.enums.ExecutionStatus;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.execution.mapper.ExecutionMapper;
import viettel.dac.backend.execution.repository.ExecutionResultRepository;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final ToolTemplateRepository toolTemplateRepository;
    private final ExecutionResultRepository executionResultRepository;
    private final ExecutionMapper executionMapper;
    private final ExecutionEngine executionEngine;

    @Value("${tool-template.execution.default-timeout:60000}")
    private int defaultTimeoutMs;

    @Transactional
    public ExecutionResultResponseDto executeTemplate(ExecutionRequestDto requestDto, UUID userId) {
        // Check if the template exists
        UUID templateId = requestDto.getTemplateId();
        if (!toolTemplateRepository.existsById(templateId)) {
            throw new ResourceNotFoundException("Template not found with ID: " + templateId);
        }

        // Create an execution record
        ExecutionResult execution = new ExecutionResult();
        execution.setTemplateId(templateId);
        execution.setUserId(userId);
        execution.setStatus(ExecutionStatus.PENDING);

        // Save the execution record
        ExecutionResult savedExecution = executionResultRepository.save(execution);

        // Start the execution asynchronously
        executionEngine.execute(
                savedExecution.getId(),
                templateId,
                requestDto.getParameters());

        // Return the pending execution
        return executionMapper.toDto(savedExecution);
    }

    /**
     * Get the result of an execution.
     *
     * @param executionId The ID of the execution
     * @return The execution result
     * @throws ResourceNotFoundException If the execution is not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "executions", key = "#executionId")
    public ExecutionResultResponseDto getExecutionResult(UUID executionId) {
        ExecutionResult execution = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        return executionMapper.toDto(execution);
    }

    /**
     * Get all execution results with optional filtering.
     *
     * @param templateId Filter by template ID (optional)
     * @param userId Filter by user ID (optional)
     * @param status Filter by status (optional)
     * @param pageable Pagination information
     * @return A page of execution results
     */
    @Transactional(readOnly = true)
    public Page<ExecutionResultResponseDto> getAllExecutionResults(
            UUID templateId,
            UUID userId,
            ExecutionStatus status,
            Pageable pageable) {

        Page<ExecutionResult> executions;

        if (templateId != null || userId != null || status != null) {
            // Apply filters if provided
            executions = executionResultRepository.findByFilters(templateId, userId, status, pageable);
        } else {
            // No filters, get all executions
            executions = executionResultRepository.findAll(pageable);
        }

        return executions.map(executionMapper::toDto);
    }

    @Transactional
    @CacheEvict(value = "executions", key = "#executionId")
    public ExecutionResultResponseDto cancelExecution(UUID executionId) {
        ExecutionResult execution = executionResultRepository.findById(executionId)
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
            ExecutionResult updatedExecution = executionResultRepository.save(execution);
            return executionMapper.toDto(updatedExecution);
        } else {
            // If cancellation failed but execution is still running, mark it as cancelled anyway
            if (execution.getStatus() == ExecutionStatus.RUNNING || execution.getStatus() == ExecutionStatus.PENDING) {
                execution.markAsCancelled();
                ExecutionResult updatedExecution = executionResultRepository.save(execution);
                return executionMapper.toDto(updatedExecution);
            }
            throw new ExecutionException("Failed to cancel execution with ID: " + executionId);
        }
    }

    @Transactional
    @CacheEvict(value = "executions", key = "#executionId")
    public void deleteExecutionResult(UUID executionId) {
        if (!executionResultRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found with ID: " + executionId);
        }

        executionResultRepository.deleteById(executionId);
    }

    @Transactional
    public int cleanUpOldExecutions(int retentionDays) {
        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        // Find executions to delete
        var oldExecutions = executionResultRepository.findByEndTimeBefore(cutoffDate);
        int count = oldExecutions.size();

        if (!oldExecutions.isEmpty()) {
            executionResultRepository.deleteByEndTimeBefore(cutoffDate);
            log.info("Deleted {} old execution results", count);
        }

        return count;
    }
}