package viettel.dac.backend.execution.engine.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.template.entity.ToolTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DefaultExecutionStrategy implements ExecutionStrategy {

    private final Map<UUID, Boolean> cancelledExecutions = new ConcurrentHashMap<>();

    @Override
    public String getTemplateType() {
        return "DEFAULT";
    }

    @Override
    public void validate(ToolTemplate template, Map<String, Object> parameters) throws ExecutionException {
        // In Phase 1, we just do basic validation
        if (template == null) {
            throw new ExecutionException("Template cannot be null");
        }

        // Check if the template has required properties
        if (template.getProperties() == null || template.getProperties().isEmpty()) {
            throw new ExecutionException("Template has no properties defined");
        }
    }

    @Override
    public ExecutionResult execute(ToolTemplate template, Map<String, Object> parameters, ExecutionResult execution) throws ExecutionException {
        try {
            // Mark as running
            execution.markAsRunning();

            // This is just a placeholder for actual execution logic
            // In real implementation, we'd execute different template types differently

            // Here we're just simulating an execution with a delay
            Thread.sleep(1000);

            // Check if the execution was cancelled
            if (cancelledExecutions.getOrDefault(execution.getId(), false)) {
                execution.markAsCancelled();
                cancelledExecutions.remove(execution.getId());
                return execution;
            }

            // For now, just generate a mock result
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Execution completed successfully");
            result.put("timestamp", Instant.now().toString());

            // Add some metrics
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("executionTimeMs", 1000);
            metrics.put("resourceUsage", "low");
            execution.setMetrics(metrics);

            // Mark as completed
            execution.markAsCompleted(result);

            return execution;

        } catch (InterruptedException e) {
            // Handle interruption (typically from timeout or cancellation)
            log.warn("Execution interrupted: {}", e.getMessage());
            execution.markAsCancelled();
            Thread.currentThread().interrupt();
            return execution;

        } catch (Exception e) {
            // Handle any other exceptions
            log.error("Error during execution: {}", e.getMessage(), e);
            execution.markAsFailed(e.getMessage());
            return execution;
        } finally {
            // Clean up
            cancelledExecutions.remove(execution.getId());
        }
    }

    @Override
    @Async("executionTaskExecutor")
    public CompletableFuture<ExecutionResult> executeAsync(ToolTemplate template, Map<String, Object> parameters, ExecutionResult execution) {
        try {
            ExecutionResult result = execute(template, parameters, execution);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Error in async execution: {}", e.getMessage(), e);
            execution.markAsFailed(e.getMessage());
            return CompletableFuture.completedFuture(execution);
        }
    }

    @Override
    public boolean cancelExecution(UUID executionId) {
        cancelledExecutions.put(executionId, true);
        return true;
    }
}