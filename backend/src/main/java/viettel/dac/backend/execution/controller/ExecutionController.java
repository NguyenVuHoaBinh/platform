package viettel.dac.backend.execution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.dto.ExecutionResultResponseDto;
import viettel.dac.backend.execution.enums.ExecutionStatus;
import viettel.dac.backend.execution.service.ExecutionService;
import viettel.dac.backend.security.model.UserDetailsImpl;

import java.util.UUID;


@RestController
@RequestMapping("/v1/executions")
@RequiredArgsConstructor
@Tag(name = "Executions", description = "API for executing templates and managing results")
@SecurityRequirement(name = "bearer-jwt")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    @Operation(
            summary = "Execute a template",
            description = "Executes a template with the provided parameters"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ExecutionResultResponseDto> executeTemplate(
            @Parameter(description = "Execution request", required = true)
            @Valid @RequestBody ExecutionRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ExecutionResultResponseDto result = executionService.executeTemplate(requestDto, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get execution result",
            description = "Retrieves the result of a specific execution"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN') and @executionSecurityService.canAccessExecution(#id, authentication.principal.id)")
    public ResponseEntity<ExecutionResultResponseDto> getExecutionResult(
            @Parameter(description = "Execution ID", required = true) @PathVariable UUID id) {

        ExecutionResultResponseDto result = executionService.getExecutionResult(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(
            summary = "Get all execution results",
            description = "Retrieves all execution results with optional filtering and pagination"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ExecutionResultResponseDto>> getAllExecutionResults(
            @Parameter(description = "Filter by template ID")
            @RequestParam(required = false) UUID templateId,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ExecutionStatus status,
            Pageable pageable) {

        Page<ExecutionResultResponseDto> results = executionService.getAllExecutionResults(
                templateId, userId, status, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/my-executions")
    @Operation(
            summary = "Get current user's execution results",
            description = "Retrieves execution results for the current user"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ExecutionResultResponseDto>> getMyExecutionResults(
            @Parameter(description = "Filter by template ID")
            @RequestParam(required = false) UUID templateId,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ExecutionStatus status,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {

        Page<ExecutionResultResponseDto> results = executionService.getAllExecutionResults(
                templateId, userDetails.getId(), status, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel execution",
            description = "Cancels an ongoing execution"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN') and @executionSecurityService.canAccessExecution(#id, authentication.principal.id)")
    public ResponseEntity<ExecutionResultResponseDto> cancelExecution(
            @Parameter(description = "Execution ID", required = true) @PathVariable UUID id) {

        ExecutionResultResponseDto result = executionService.cancelExecution(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete execution result",
            description = "Deletes an execution result by its ID (requires ADMIN role or ownership of the execution)"
    )
    @ApiResponse(responseCode = "204", description = "Execution result successfully deleted")
    @PreAuthorize("hasRole('ADMIN') or @executionSecurityService.canAccessExecution(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteExecutionResult(
            @Parameter(description = "Execution ID", required = true) @PathVariable UUID id) {

        executionService.deleteExecutionResult(id);
        return ResponseEntity.noContent().build();
    }
}