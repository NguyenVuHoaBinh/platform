package viettel.dac.backend.execution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.execution.dto.ApiExecutionResultResponseDto;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.service.ApiExecutionService;
import viettel.dac.backend.security.model.UserDetailsImpl;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api-executions")
@RequiredArgsConstructor
@Tag(name = "API Executions", description = "API for executing API templates and managing results")
@SecurityRequirement(name = "bearer-jwt")
public class ApiExecutionController {

    private final ApiExecutionService apiExecutionService;

    @PostMapping
    @Operation(
            summary = "Execute an API template",
            description = "Executes an API template with the provided parameters"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiExecutionResultResponseDto> executeApiTemplate(
            @Parameter(description = "Execution request", required = true)
            @Valid @RequestBody ExecutionRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ApiExecutionResultResponseDto result = apiExecutionService.executeApiTemplate(requestDto, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get API execution result",
            description = "Retrieves the result of a specific API execution"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN') and @executionSecurityService.canAccessExecution(#id, authentication.principal.id)")
    public ResponseEntity<ApiExecutionResultResponseDto> getApiExecutionResult(
            @Parameter(description = "Execution ID", required = true)
            @PathVariable UUID id) {

        ApiExecutionResultResponseDto result = apiExecutionService.getApiExecutionResult(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/template/{templateId}")
    @Operation(
            summary = "Get API executions by template",
            description = "Retrieves API execution results for a specific template (requires MANAGER or ADMIN role)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResultResponseDto>> getApiExecutionsByTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable UUID templateId,
            Pageable pageable) {

        Page<ApiExecutionResultResponseDto> results =
                apiExecutionService.getApiExecutionResultsByTemplate(templateId, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/successful")
    @Operation(
            summary = "Get API executions by success status",
            description = "Retrieves API execution results filtered by success status (requires MANAGER or ADMIN role)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResultResponseDto>> getApiExecutionsBySuccess(
            @Parameter(description = "Success status")
            @RequestParam(defaultValue = "true") boolean successful,
            Pageable pageable) {

        Page<ApiExecutionResultResponseDto> results =
                apiExecutionService.getApiExecutionResultsBySuccess(successful, pageable);
        return ResponseEntity.ok(results);
    }


    @GetMapping("/status-code/{statusCode}")
    @Operation(
            summary = "Get API executions by status code",
            description = "Retrieves API execution results filtered by HTTP status code (requires MANAGER or ADMIN role)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResultResponseDto>> getApiExecutionsByStatusCode(
            @Parameter(description = "HTTP Status Code", required = true)
            @PathVariable int statusCode,
            Pageable pageable) {

        Page<ApiExecutionResultResponseDto> results =
                apiExecutionService.getApiExecutionResultsByStatusCode(statusCode, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/response-time")
    @Operation(
            summary = "Get API executions by response time",
            description = "Retrieves API execution results with response time greater than the threshold (requires MANAGER or ADMIN role)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResultResponseDto>> getApiExecutionsByResponseTime(
            @Parameter(description = "Response time threshold in milliseconds")
            @RequestParam(defaultValue = "1000") long thresholdMs,
            Pageable pageable) {

        Page<ApiExecutionResultResponseDto> results =
                apiExecutionService.getApiExecutionResultsByResponseTime(thresholdMs, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/my-executions")
    @Operation(
            summary = "Get current user's API execution results",
            description = "Retrieves API execution results for the current user's executions"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResultResponseDto>> getMyApiExecutions(
            @Parameter(description = "Template ID")
            @RequestParam(required = false) UUID templateId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {

        Page<ApiExecutionResultResponseDto> results =
                apiExecutionService.getApiExecutionResultsByUserAndTemplate(
                        userDetails.getId(), templateId, pageable);
        return ResponseEntity.ok(results);
    }
}