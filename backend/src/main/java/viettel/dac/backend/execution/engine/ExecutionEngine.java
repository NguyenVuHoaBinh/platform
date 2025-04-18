package viettel.dac.backend.execution.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.repository.ExecutionRepository;
import viettel.dac.backend.plugin.PluginService;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.repository.TemplateRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ExecutionEngine {

    private final TemplateRepository templateRepository;
    private final ExecutionRepository executionRepository;
    private final PluginService pluginService;

    @Autowired
    public ExecutionEngine(
            TemplateRepository templateRepository,
            ExecutionRepository executionRepository,
            PluginService pluginService) {
        this.templateRepository = templateRepository;
        this.executionRepository = executionRepository;
        this.pluginService = pluginService;
    }

    /**
     * Execute a template with the given parameters.
     */
    public CompletableFuture<BaseExecution> execute(UUID executionId, UUID templateId, Map<String, Object> parameters) {
        // Get the template
        BaseTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + templateId));

        // Get the execution record
        BaseExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + executionId));

        // Get the appropriate execution strategy for the template type through plugin system
        ExecutionStrategy strategy = pluginService.getExecutionStrategy(template.getTemplateType().name());

        // Validate the template and parameters
        strategy.validate(template, parameters);

        // Execute the template asynchronously
        return strategy.executeAsync(template, parameters, execution)
                .thenApply(executionRepository::save)
                .exceptionally(ex -> {
                    log.error("Error during execution: {}", ex.getMessage(), ex);
                    execution.markAsFailed(ex.getMessage());
                    return executionRepository.save(execution);
                });
    }

    /**
     * Cancel an ongoing execution.
     */
    public boolean cancelExecution(UUID executionId) {
        // Get the execution record
        BaseExecution execution = executionRepository.findById(executionId)
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