package viettel.dac.backend.execution.engine;


import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.template.entity.ToolTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface ExecutionStrategy {

    String getTemplateType();
    void validate(ToolTemplate template, Map<String, Object> parameters) throws ExecutionException;
    ExecutionResult execute(ToolTemplate template, Map<String, Object> parameters, ExecutionResult execution) throws ExecutionException;
    CompletableFuture<ExecutionResult> executeAsync(ToolTemplate template, Map<String, Object> parameters, ExecutionResult execution);
    boolean cancelExecution(UUID executionId);
}