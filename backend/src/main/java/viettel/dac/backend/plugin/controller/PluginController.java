package viettel.dac.backend.plugin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.plugin.PluginDescriptor;
import viettel.dac.backend.plugin.service.PluginService;
import viettel.dac.backend.plugin.service.PluginTemplateService;
import viettel.dac.backend.security.model.UserDetailsImpl;
import viettel.dac.backend.template.dto.TemplateResponseDto;

import java.util.List;
import java.util.Map;

/**
 * REST controller for plugin-related operations.
 */
@RestController
@RequestMapping("/api/v1/plugins")
@RequiredArgsConstructor
@Tag(name = "Plugins", description = "API for plugin management and template creation via plugins")
@SecurityRequirement(name = "bearer-jwt")
public class PluginController {

    private final PluginService pluginService;
    private final PluginTemplateService pluginTemplateService;

    @GetMapping("/types")
    @Operation(
            summary = "Get all plugin types",
            description = "Retrieves all registered plugin types"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<String>> getPluginTypes() {
        return ResponseEntity.ok(pluginService.getAllPluginTypes());
    }


    @GetMapping
    @Operation(
            summary = "Get all plugins",
            description = "Retrieves information about all registered plugins"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<PluginDescriptor>> getPlugins() {
        return ResponseEntity.ok(pluginService.getAllPluginDescriptors());
    }

    @PostMapping("/templates")
    @Operation(
            summary = "Create a template",
            description = "Creates a template using the specified plugin type"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TemplateResponseDto> createTemplate(
            @Parameter(description = "Plugin type", required = true)
            @RequestParam String type,

            @Parameter(description = "Template data", required = true)
            @Valid @RequestBody Map<String, Object> templateData,

            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        TemplateResponseDto template = pluginTemplateService.createTemplate(
                type, templateData, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate template data",
            description = "Validates template data for the specified plugin type"
    )
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Boolean> validateTemplate(
            @Parameter(description = "Plugin type", required = true)
            @RequestParam String type,

            @Parameter(description = "Template data", required = true)
            @RequestBody Map<String, Object> templateData) {

        boolean valid = pluginService.validateTemplate(type, templateData);
        return ResponseEntity.ok(valid);
    }
}