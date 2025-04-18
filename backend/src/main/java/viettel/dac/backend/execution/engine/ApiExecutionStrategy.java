package viettel.dac.backend.execution.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import viettel.dac.backend.execution.entity.ApiExecution;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.exception.ExecutionException;
import viettel.dac.backend.execution.exception.InvalidParameterException;
import viettel.dac.backend.execution.exception.TimeoutException;
import viettel.dac.backend.execution.repository.ApiExecutionRepository;
import viettel.dac.backend.execution.util.ParameterSubstitutionUtil;
import viettel.dac.backend.template.entity.ApiTemplate;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpMethod.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiExecutionStrategy implements ExecutionStrategy {

    private final ApiExecutionRepository apiExecutionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ParameterSubstitutionUtil parameterSubstitutionUtil;

    private final Map<UUID, Boolean> cancelledExecutions = new ConcurrentHashMap<>();

    @Override
    public String getTemplateType() {
        return TemplateType.API.name();
    }

    @Override
    public void validate(BaseTemplate template, Map<String, Object> parameters) throws ExecutionException {
        if (template == null) {
            throw new InvalidParameterException("Template cannot be null");
        }

        if (!(template instanceof ApiTemplate)) {
            throw new InvalidParameterException("Template is not an API template");
        }

        ApiTemplate apiTemplate = (ApiTemplate) template;

        // Validate endpoint
        if (apiTemplate.getEndpoint() == null || apiTemplate.getEndpoint().trim().isEmpty()) {
            throw new InvalidParameterException("Endpoint is required for API template");
        }

        // Validate HTTP method
        if (apiTemplate.getHttpMethod() == null) {
            throw new InvalidParameterException("HTTP method is required for API template");
        }

        // Try to substitute parameters to validate they exist
        try {
            substituteParameters(apiTemplate, parameters);
        } catch (Exception e) {
            throw new InvalidParameterException("Error substituting parameters: " + e.getMessage());
        }
    }

    @Override
    public BaseExecution execute(BaseTemplate template, Map<String, Object> parameters, BaseExecution execution) throws ExecutionException {
        if (!(template instanceof ApiTemplate)) {
            throw new InvalidParameterException("Template is not an API template");
        }

        try {
            ApiTemplate apiTemplate = (ApiTemplate) template;

            // Create or get ApiExecution instance
            ApiExecution apiExecution;
            if (execution instanceof ApiExecution) {
                apiExecution = (ApiExecution) execution;
            } else {
                // If we have a base execution, create a new API execution with the same ID
                apiExecution = new ApiExecution();
                apiExecution.setId(execution.getId());
                apiExecution.setTemplateId(execution.getTemplateId());
                apiExecution.setUserId(execution.getUserId());
                apiExecution.setStatus(execution.getStatus());
            }

            // Mark as running
            apiExecution.markAsRunning();

            // Check if the execution is already cancelled
            if (cancelledExecutions.getOrDefault(apiExecution.getId(), false)) {
                apiExecution.markAsCancelled();
                cancelledExecutions.remove(apiExecution.getId());
                return apiExecution;
            }

            // Substitute parameters in the template
            ApiRequestContext requestContext = substituteParameters(apiTemplate, parameters);

            // Execute the HTTP request
            Instant startTime = Instant.now();
            ResponseEntity<String> response;

            try {
                response = executeHttpRequest(requestContext);
            } catch (TimeoutException e) {
                apiExecution.markAsTimedOut();
                return apiExecution;
            }

            // Calculate response time
            long responseTimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            // Check if the execution was cancelled during the request
            if (cancelledExecutions.getOrDefault(apiExecution.getId(), false)) {
                apiExecution.markAsCancelled();
                cancelledExecutions.remove(apiExecution.getId());
                return apiExecution;
            }

            // Process the response
            int statusCode = response.getStatusCodeValue();
            boolean successful = response.getStatusCode().is2xxSuccessful();

            // Extract response headers
            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach((name, values) -> {
                if (!values.isEmpty()) {
                    responseHeaders.put(name, String.join(", ", values));
                }
            });

            // Parse response body
            Object responseBody = response.getBody();
            if (responseBody != null && response.getHeaders().getContentType() != null &&
                    response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                try {
                    responseBody = objectMapper.readValue(response.getBody(), Object.class);
                } catch (Exception e) {
                    log.warn("Error parsing JSON response: {}", e.getMessage());
                    // Keep response as string if parsing fails
                }
            }

            // Update execution properties
            apiExecution.setStatusCode(statusCode);
            apiExecution.setResponseHeaders(responseHeaders);
            apiExecution.setResponseBody(responseBody);
            apiExecution.setResponseTimeMs(responseTimeMs);
            apiExecution.setSuccessful(successful);

            // Set metrics
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("responseTimeMs", responseTimeMs);
            metrics.put("statusCode", statusCode);
            metrics.put("successful", successful);
            apiExecution.setMetrics(metrics);

            // Generate result object
            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", statusCode);
            result.put("successful", successful);
            result.put("responseTimeMs", responseTimeMs);

            // Mark as completed
            apiExecution.markAsCompleted(result);

            // Save and return
            return apiExecutionRepository.save(apiExecution);

        } catch (Exception e) {
            // Handle other exceptions
            log.error("Error executing API template: {}", e.getMessage(), e);
            execution.markAsFailed(e.getMessage());
            return execution;
        } finally {
            // Clean up
            cancelledExecutions.remove(execution.getId());
        }
    }

    @Override
    @Async("executionTaskExecutor")
    public CompletableFuture<BaseExecution> executeAsync(BaseTemplate template, Map<String, Object> parameters, BaseExecution execution) {
        try {
            BaseExecution result = execute(template, parameters, execution);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Error in async API execution: {}", e.getMessage(), e);
            execution.markAsFailed(e.getMessage());
            return CompletableFuture.completedFuture(execution);
        }
    }

    @Override
    public boolean cancelExecution(UUID executionId) {
        cancelledExecutions.put(executionId, true);
        return true;
    }

    private ApiRequestContext substituteParameters(ApiTemplate apiTemplate, Map<String, Object> parameters) {
        ApiRequestContext context = new ApiRequestContext();

        // Substitute in endpoint
        context.setEndpoint(parameterSubstitutionUtil.substituteString(apiTemplate.getEndpoint(), parameters));

        // Substitute in headers
        if (apiTemplate.getHeaders() != null && !apiTemplate.getHeaders().isEmpty()) {
            context.setHeaders(parameterSubstitutionUtil.substituteMap(apiTemplate.getHeaders(), parameters));
        } else {
            context.setHeaders(new HashMap<>());
        }

        // Add content type header if specified
        if (apiTemplate.getContentType() != null && !apiTemplate.getContentType().isEmpty()) {
            context.getHeaders().put(HttpHeaders.CONTENT_TYPE, apiTemplate.getContentType());
        }

        // Set HTTP method
        context.setHttpMethod(apiTemplate.getHttpMethod());

        // Substitute in query parameters
        if (apiTemplate.getQueryParams() != null && !apiTemplate.getQueryParams().isEmpty()) {
            Map<String, Object> queryParams = new HashMap<>();
            apiTemplate.getQueryParams().forEach((key, value) -> {
                String newKey = parameterSubstitutionUtil.substituteString(key, parameters);
                Object newValue;
                if (value instanceof String) {
                    newValue = parameterSubstitutionUtil.substituteString((String) value, parameters);
                } else {
                    newValue = parameterSubstitutionUtil.substituteJson(value, parameters);
                }
                queryParams.put(newKey, newValue);
            });
            context.setQueryParams(queryParams);
        }

        // Substitute in request body
        if (apiTemplate.getRequestBody() != null) {
            context.setRequestBody(parameterSubstitutionUtil.substituteJson(apiTemplate.getRequestBody(), parameters));
        }

        // Set timeout and follow redirects
        context.setTimeout(apiTemplate.getTimeout());
        context.setFollowRedirects(apiTemplate.getFollowRedirects());

        return context;
    }

    private ResponseEntity<String> executeHttpRequest(ApiRequestContext context) throws ExecutionException, TimeoutException {
        try {
            // Build the URI with query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(context.getEndpoint());
            if (context.getQueryParams() != null && !context.getQueryParams().isEmpty()) {
                context.getQueryParams().forEach((key, value) -> {
                    if (value != null) {
                        uriBuilder.queryParam(key, value.toString());
                    }
                });
            }
            URI uri = uriBuilder.build().toUri();

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            if (context.getHeaders() != null && !context.getHeaders().isEmpty()) {
                context.getHeaders().forEach(headers::add);
            }

            // Create request entity
            HttpEntity<?> requestEntity;
            if (context.getRequestBody() != null) {
                requestEntity = new HttpEntity<>(context.getRequestBody(), headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            // Execute request
            org.springframework.http.HttpMethod httpMethod = mapHttpMethod(context.getHttpMethod());
            return restTemplate.exchange(uri, httpMethod, requestEntity, String.class);

        } catch (ResourceAccessException e) {
            // Handle connection or timeout issues
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                throw new TimeoutException("API request timed out: " + e.getMessage(), e);
            } else {
                throw new ExecutionException("Error connecting to API: " + e.getMessage(), e);
            }
        } catch (HttpStatusCodeException e) {
            // For non-2xx responses, return the response with error details
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ExecutionException("Error executing API request: " + e.getMessage(), e);
        }
    }

    private org.springframework.http.HttpMethod mapHttpMethod(HttpMethod method) {
        return switch (method) {
            case GET -> GET;
            case POST -> org.springframework.http.HttpMethod.POST;
            case PUT -> org.springframework.http.HttpMethod.PUT;
            case DELETE -> org.springframework.http.HttpMethod.DELETE;
            case PATCH -> org.springframework.http.HttpMethod.PATCH;
            case HEAD -> org.springframework.http.HttpMethod.HEAD;
            case OPTIONS -> OPTIONS;
        };
    }

    @Getter
    @Setter
    private static class ApiRequestContext {
        private String endpoint;
        private HttpMethod httpMethod;
        private Map<String, String> headers;
        private Map<String, Object> queryParams;
        private Object requestBody;
        private Integer timeout;
        private Boolean followRedirects;
    }
}