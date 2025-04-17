package viettel.dac.backend.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.template.dto.TemplateCreateDto;
import viettel.dac.backend.template.dto.TemplateResponseDto;
import viettel.dac.backend.template.dto.TemplateUpdateDto;
import viettel.dac.backend.template.enums.TemplateType;
import viettel.dac.backend.template.service.TemplateService;

import java.util.*;

@RestController
@RequestMapping("/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "API for managing tool templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(
            summary = "Get all templates",
            description = "Retrieves all templates with optional filtering and pagination"
    )
    public ResponseEntity<Page<TemplateResponseDto>> getAllTemplates(
            @Parameter(description = "Filter by name containing") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by template type") @RequestParam(required = false) TemplateType type,
            @Parameter(description = "Filter by tags") @RequestParam(required = false) Set<String> tags,
            @Parameter(description = "Filter by creator") @RequestParam(required = false) UUID createdBy,
            Pageable pageable) {

        Page<TemplateResponseDto> templates = templateService.getAllTemplates(name, type, tags, createdBy, pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get template by ID",
            description = "Retrieves a specific template by its ID"
    )
    public ResponseEntity<TemplateResponseDto> getTemplateById(
            @Parameter(description = "Template ID", required = true) @PathVariable UUID id) {

        TemplateResponseDto template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PostMapping
    @Operation(
            summary = "Create a new template",
            description = "Creates a new template with the provided data"
    )
    public ResponseEntity<TemplateResponseDto> createTemplate(
            @Parameter(description = "Template data", required = true) @Valid @RequestBody TemplateCreateDto createDto) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        TemplateResponseDto createdTemplate = templateService.createTemplate(createDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a template",
            description = "Updates an existing template with the provided data"
    )
    public ResponseEntity<TemplateResponseDto> updateTemplate(
            @Parameter(description = "Template ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated template data", required = true) @Valid @RequestBody TemplateUpdateDto updateDto) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        TemplateResponseDto updatedTemplate = templateService.updateTemplate(id, updateDto, userId);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a template",
            description = "Deletes a template by its ID"
    )
    @ApiResponse(responseCode = "204", description = "Template successfully deleted")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "Template ID", required = true) @PathVariable UUID id) {

        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/versions")
    @Operation(
            summary = "Get template versions",
            description = "Retrieves all versions of a specific template"
    )
    public ResponseEntity<List<TemplateResponseDto>> getTemplateVersions(
            @Parameter(description = "Template ID", required = true) @PathVariable UUID id) {

        List<TemplateResponseDto> versions = templateService.getTemplateVersions(id);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(
            summary = "Get specific template version",
            description = "Retrieves a specific version of a template"
    )
    public ResponseEntity<TemplateResponseDto> getTemplateVersion(
            @Parameter(description = "Template ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Version string", required = true) @PathVariable String version) {

        TemplateResponseDto templateVersion = templateService.getTemplateVersion(id, version);
        return ResponseEntity.ok(templateVersion);
    }

    @GetMapping("/tags")
    @Operation(
            summary = "Get all tags",
            description = "Retrieves all distinct tags used across templates"
    )
    public ResponseEntity<List<String>> getAllTags() {
        List<String> tags = templateService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}
