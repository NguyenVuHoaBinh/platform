package viettel.dac.backend.execution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.execution.dto.ExecutionRequestDto;
import viettel.dac.backend.execution.dto.ExecutionResultResponseDto;
import viettel.dac.backend.execution.enums.ExecutionStatus;
import viettel.dac.backend.execution.service.ExecutionService;

import java.util.UUID;


@RestController
@RequestMapping("/v1/executions")
@RequiredArgsConstructor
@Tag(name = "Executions", description = "API for executing templates and managing results")
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    @Operation(
            summary = "Execute a template",
            description = "Executes a template with the provided parameters"
    )
    public ResponseEntity<ExecutionResultResponseDto> executeTemplate(
            @Parameter(description = "Execution request", required = true) @Valid @RequestBody ExecutionRequestDto requestDto) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        ExecutionResultResponseDto result = executionService.executeTemplate(requestDto, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get execution result",
            description = "Retrieves the result of a specific execution"
    )
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
    public ResponseEntity<Page<ExecutionResultResponseDto>> getAllExecutionResults(
            @Parameter(description = "Filter by template ID") @RequestParam(required = false) UUID templateId,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) ExecutionStatus status,
            Pageable pageable) {

        Page<ExecutionResultResponseDto> results = executionService.getAllExecutionResults(
                templateId, userId, status, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel execution",
            description = "Cancels an ongoing execution"
    )
    public ResponseEntity<ExecutionResultResponseDto> cancelExecution(
            @Parameter(description = "Execution ID", required = true) @PathVariable UUID id) {

        ExecutionResultResponseDto result = executionService.cancelExecution(id);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete execution result",
            description = "Deletes an execution result by its ID"
    )
    @ApiResponse(responseCode = "204", description = "Execution result successfully deleted")
    public ResponseEntity<Void> deleteExecutionResult(
            @Parameter(description = "Execution ID", required = true) @PathVariable UUID id) {

        executionService.deleteExecutionResult(id);
        return ResponseEntity.noContent().build();
    }
}