package viettel.dac.backend.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.template.dto.ApiToolCreateDto;
import viettel.dac.backend.template.dto.ApiToolResponseDto;
import viettel.dac.backend.template.dto.ApiToolUpdateDto;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.service.ApiToolService;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api-templates")
@RequiredArgsConstructor
@Tag(name = "API Templates", description = "API for managing API tool templates")
public class ApiToolController {

    private final ApiToolService apiToolService;

    @PostMapping
    @Operation(
            summary = "Create a new API tool template",
            description = "Creates a new API tool template with the provided data"
    )
    public ResponseEntity<ApiToolResponseDto> createApiTemplate(
            @Parameter(description = "API template data", required = true)
            @Valid @RequestBody ApiToolCreateDto createDto) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        ApiToolResponseDto createdTemplate = apiToolService.createApiTemplate(createDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get API template by ID",
            description = "Retrieves a specific API template by its ID"
    )
    public ResponseEntity<ApiToolResponseDto> getApiTemplateById(
            @Parameter(description = "API Template ID", required = true)
            @PathVariable UUID id) {

        ApiToolResponseDto template = apiToolService.getApiTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an API template",
            description = "Updates an existing API template with the provided data"
    )
    public ResponseEntity<ApiToolResponseDto> updateApiTemplate(
            @Parameter(description = "API Template ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated API template data", required = true)
            @Valid @RequestBody ApiToolUpdateDto updateDto) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        ApiToolResponseDto updatedTemplate = apiToolService.updateApiTemplate(id, updateDto, userId);
        return ResponseEntity.ok(updatedTemplate);
    }

    @GetMapping("/method/{method}")
    @Operation(
            summary = "Get API templates by HTTP method",
            description = "Retrieves API templates filtered by HTTP method"
    )
    public ResponseEntity<Page<ApiToolResponseDto>> getApiTemplatesByHttpMethod(
            @Parameter(description = "HTTP Method", required = true)
            @PathVariable HttpMethod method,
            Pageable pageable) {

        Page<ApiToolResponseDto> templates = apiToolService.getApiTemplatesByHttpMethod(method, pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/domain")
    @Operation(
            summary = "Get API templates by domain",
            description = "Retrieves API templates filtered by domain in the endpoint"
    )
    public ResponseEntity<Page<ApiToolResponseDto>> getApiTemplatesByDomain(
            @Parameter(description = "Domain", required = true)
            @RequestParam String domain,
            Pageable pageable) {

        Page<ApiToolResponseDto> templates = apiToolService.getApiTemplatesByDomain(domain, pageable);
        return ResponseEntity.ok(templates);
    }
}
