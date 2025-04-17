package viettel.dac.backend.execution.engine.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.enums.TemplateType;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The execution engine is responsible for executing templates.
 * It uses a strategy pattern to delegate the actual execution to
 * appropriate strategy implementations based on the template type.
 */
@Component
@Slf4j
public class ExecutionEngine {

    private final ToolTemplateRepository toolTemplateRepository;
    private final ExecutionResultRepository executionResultRepository;
    private final Map<String, ExecutionStrategy> strategies = new HashMap<>();
    private final ExecutionStrategy defaultStrategy;

    @Autowired
    public ExecutionEngine(
            ToolTemplateRepository toolTemplateRepository,
            ExecutionResultRepository executionResultRepository,
            List<ExecutionStrategy> strategyList) {

        this.toolTemplateRepository = toolTemplateRepository;
        this.executionResultRepository = executionResultRepository;

        // Register strategies by type
        ExecutionStrategy foundDefaultStrategy = null;
        for (ExecutionStrategy strategy : strategyList) {
            strategies.put(strategy.getTemplateType(), strategy);
            if ("DEFAULT".equals(strategy.getTemplateType())) {
                foundDefaultStrategy = strategy;
            }
        }

        // Set default strategy
        this.defaultStrategy = foundDefaultStrategy;
        if (this.defaultStrategy == null) {
            throw new IllegalStateException("No default execution strategy found");
        }
    }

    /**
     * Execute a template with the given parameters.
     *
     * @param executionId The ID of the execution record
     * @param templateId The ID of the template to execute
     * @param parameters The parameters for the execution
     * @return A CompletableFuture that will complete with the execution result
     * @throws ResourceNotFoundException If the template or execution is not found
     * @throws ExecutionException If there's an error during execution
     */
    public CompletableFuture<ExecutionResult> execute(UUID executionId, UUID templateId, Map<String, Object> parameters) {
        // Get the template
        ToolTemplate template = toolTemplateRepository.findByIdAndActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + templateId));

        // Get the execution record
        ExecutionResult execution = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Get the appropriate strategy for the template type
        ExecutionStrategy strategy = getStrategyForTemplate(template);

        // Validate the template and parameters
        strategy.validate(template, parameters);

        // Execute the template asynchronously
        return strategy.executeAsync(template, parameters, execution)
                .thenApply(executionResultRepository::save)
                .exceptionally(ex -> {
                    log.error("Error during execution: {}", ex.getMessage(), ex);
                    execution.markAsFailed(ex.getMessage());
                    return executionResultRepository.save(execution);
                });
    }

    /**
     * Cancel an ongoing execution.
     *
     * @param executionId The ID of the execution to cancel
     * @return true if the execution was cancelled, false otherwise
     * @throws ResourceNotFoundException If the execution is not found
     */
    public boolean cancelExecution(UUID executionId) {
        // Get the execution record
        ExecutionResult execution = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Use all strategies to try to cancel the execution
        // This is in case we don't know which strategy was used
        for (ExecutionStrategy strategy : strategies.values()) {
            if (strategy.cancelExecution(executionId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the appropriate strategy for the given template.
     *
     * @param template The template
     * @return The execution strategy
     */
    private ExecutionStrategy getStrategyForTemplate(ToolTemplate template) {
        TemplateType type = template.getTemplateType();
        ExecutionStrategy strategy = strategies.get(type.name());

        if (strategy == null) {
            log.warn("No specific strategy found for template type {}, using default strategy", type);
            return defaultStrategy;
        }

        return strategy;
    }
}