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
import viettel.dac.backend.execution.dto.ApiExecutionResponseDto;
import viettel.dac.backend.execution.dto.ApiExecutionSearchFilterDto;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.enums.ExecutionStatus;
import viettel.dac.backend.execution.service.ApiExecutionService;
import viettel.dac.backend.security.model.UserDetailsImpl;


import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-executions")
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
    public ResponseEntity<ApiExecutionResponseDto> executeApiTemplate(
            @Parameter(description = "Execution request", required = true)
            @Valid @RequestBody ExecutionRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ApiExecutionResponseDto result = apiExecutionService.executeApiTemplate(requestDto, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get API execution result",
            description = "Retrieves the result of a specific API execution"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN') and @executionSecurityService.canAccessExecution(#id, authentication.principal.id)")
    public ResponseEntity<ApiExecutionResponseDto> getApiExecutionResult(
            @Parameter(description = "Execution ID", required = true)
            @PathVariable UUID id) {

        ApiExecutionResponseDto result = apiExecutionService.getApiExecutionResult(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(
            summary = "Search API executions",
            description = "Search for API executions with various filters"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResponseDto>> searchApiExecutions(
            @Parameter(description = "Filter by template ID")
            @RequestParam(required = false) UUID templateId,

            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ExecutionStatus status,

            @Parameter(description = "Filter by HTTP status code")
            @RequestParam(required = false) Integer statusCode,

            @Parameter(description = "Filter by successful flag")
            @RequestParam(required = false) Boolean successful,

            @Parameter(description = "Filter by max response time (ms)")
            @RequestParam(required = false) Long maxResponseTimeMs,

            @Parameter(description = "Filter by start time from")
            @RequestParam(required = false) Instant startTimeFrom,

            @Parameter(description = "Filter by start time to")
            @RequestParam(required = false) Instant startTimeTo,

            @Parameter(description = "Filter by end time from")
            @RequestParam(required = false) Instant endTimeFrom,

            @Parameter(description = "Filter by end time to")
            @RequestParam(required = false) Instant endTimeTo,

            Pageable pageable) {

        ApiExecutionSearchFilterDto filter = ApiExecutionSearchFilterDto.builder()
                .templateId(templateId)
                .userId(userId)
                .status(status)
                .statusCode(statusCode)
                .successful(successful)
                .maxResponseTimeMs(maxResponseTimeMs)
                .startTimeFrom(startTimeFrom)
                .startTimeTo(startTimeTo)
                .endTimeFrom(endTimeFrom)
                .endTimeTo(endTimeTo)
                .build();

        Page<ApiExecutionResponseDto> results = apiExecutionService.searchApiExecutions(filter, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/my-executions")
    @Operation(
            summary = "Get current user's API execution results",
            description = "Retrieves API execution results for the current user"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResponseDto>> getMyApiExecutions(
            @Parameter(description = "Filter by template ID")
            @RequestParam(required = false) UUID templateId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ExecutionStatus status,

            @Parameter(description = "Filter by HTTP status code")
            @RequestParam(required = false) Integer statusCode,

            @Parameter(description = "Filter by successful flag")
            @RequestParam(required = false) Boolean successful,

            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {

        ApiExecutionSearchFilterDto filter = ApiExecutionSearchFilterDto.builder()
                .templateId(templateId)
                .userId(userDetails.getId())
                .status(status)
                .statusCode(statusCode)
                .successful(successful)
                .build();

        Page<ApiExecutionResponseDto> results = apiExecutionService.searchApiExecutions(filter, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/status-code/{statusCode}")
    @Operation(
            summary = "Get API executions by status code",
            description = "Retrieves API execution results filtered by HTTP status code"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResponseDto>> getApiExecutionsByStatusCode(
            @Parameter(description = "HTTP Status Code", required = true)
            @PathVariable int statusCode,
            Pageable pageable) {

        Page<ApiExecutionResponseDto> results = apiExecutionService.getApiExecutionsByStatusCode(statusCode, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/successful/{successful}")
    @Operation(
            summary = "Get API executions by success status",
            description = "Retrieves API execution results filtered by success status"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiExecutionResponseDto>> getApiExecutionsBySuccessful(
            @Parameter(description = "Success status", required = true)
            @PathVariable boolean successful,
            Pageable pageable) {

        Page<ApiExecutionResponseDto> results = apiExecutionService.getApiExecutionsBySuccessful(successful, pageable);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete API execution result",
            description = "Deletes an API execution result by its ID"
    )
    @PreAuthorize("hasRole('ADMIN') or @executionSecurityService.canDeleteExecution(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteApiExecutionResult(
            @Parameter(description = "Execution ID", required = true)
            @PathVariable UUID id) {

        apiExecutionService.deleteApiExecution(id);
        return ResponseEntity.noContent().build();
    }
}
