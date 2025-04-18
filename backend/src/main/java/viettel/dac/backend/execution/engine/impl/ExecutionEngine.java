package viettel.dac.backend.execution.engine.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.engine.ExecutionStrategy;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.repository.ExecutionResultRepository;
import viettel.dac.backend.plugin.PluginService;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.repository.ToolTemplateRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ExecutionEngine {

    private final ToolTemplateRepository toolTemplateRepository;
    private final ExecutionResultRepository executionResultRepository;
    private final PluginService pluginService;

    @Autowired
    public ExecutionEngine(
            ToolTemplateRepository toolTemplateRepository,
            ExecutionResultRepository executionResultRepository,
            PluginService pluginService) {
        this.toolTemplateRepository = toolTemplateRepository;
        this.executionResultRepository = executionResultRepository;
        this.pluginService = pluginService;
    }

    /**
     * Execute a template with the given parameters.
     */
    public CompletableFuture<ExecutionResult> execute(UUID executionId, UUID templateId, Map<String, Object> parameters) {
        // Get the template
        ToolTemplate template = toolTemplateRepository.findByIdAndActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + templateId));

        // Get the execution record
        ExecutionResult execution = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Get the appropriate execution strategy for the template type through plugin system
        ExecutionStrategy strategy = pluginService.getExecutionStrategy(template.getTemplateType().name());

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
     */
    public boolean cancelExecution(UUID executionId) {
        // Get the execution record
        ExecutionResult execution = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Try all registered plugin strategies to cancel the execution
        for (String type : pluginService.getAllPluginTypes()) {
            ExecutionStrategy strategy = pluginService.getExecutionStrategy(type);
            if (strategy.cancelExecution(executionId)) {
                return true;
            }
        }

        return false;
    }
}