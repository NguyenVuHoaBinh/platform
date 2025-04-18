package viettel.dac.backend.execution.engine;

import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.template.entity.BaseTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ExecutionStrategy {

    String getTemplateType();

    void validate(BaseTemplate template, Map<String, Object> parameters) throws ExecutionException;

    BaseExecution execute(BaseTemplate template, Map<String, Object> parameters, BaseExecution execution) throws ExecutionException;

    CompletableFuture<BaseExecution> executeAsync(BaseTemplate template, Map<String, Object> parameters, BaseExecution execution);

    boolean cancelExecution(UUID executionId);
}