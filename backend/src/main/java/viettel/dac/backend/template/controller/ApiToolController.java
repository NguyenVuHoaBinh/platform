package viettel.dac.backend.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.security.model.UserDetailsImpl;
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
@SecurityRequirement(name = "bearer-jwt")
public class ApiToolController {

    private final ApiToolService apiToolService;

    @PostMapping
    @Operation(
            summary = "Create a new API tool template",
            description = "Creates a new API tool template with the provided data (requires MANAGER or ADMIN role)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiToolResponseDto> createApiTemplate(
            @Parameter(description = "API template data", required = true)
            @Valid @RequestBody ApiToolCreateDto createDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ApiToolResponseDto createdTemplate = apiToolService.createApiTemplate(createDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get API template by ID",
            description = "Retrieves a specific API template by its ID"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiToolResponseDto> getApiTemplateById(
            @Parameter(description = "API Template ID", required = true)
            @PathVariable UUID id) {

        ApiToolResponseDto template = apiToolService.getApiTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an API template",
            description = "Updates an existing API template with the provided data (requires MANAGER or ADMIN role, or be the creator)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @templateSecurityService.isTemplateCreator(#id, authentication.principal.id)")
    public ResponseEntity<ApiToolResponseDto> updateApiTemplate(
            @Parameter(description = "API Template ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated API template data", required = true)
            @Valid @RequestBody ApiToolUpdateDto updateDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ApiToolResponseDto updatedTemplate = apiToolService.updateApiTemplate(id, updateDto, userDetails.getId());
        return ResponseEntity.ok(updatedTemplate);
    }

    @GetMapping("/method/{method}")
    @Operation(
            summary = "Get API templates by HTTP method",
            description = "Retrieves API templates filtered by HTTP method"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ApiToolResponseDto>> getApiTemplatesByDomain(
            @Parameter(description = "Domain", required = true)
            @RequestParam String domain,
            Pageable pageable) {

        Page<ApiToolResponseDto> templates = apiToolService.getApiTemplatesByDomain(domain, pageable);
        return ResponseEntity.ok(templates);
    }
}