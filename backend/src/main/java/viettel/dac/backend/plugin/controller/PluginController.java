package viettel.dac.backend.plugin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.plugin.PluginService;
import viettel.dac.backend.plugin.service.PluginTemplateService;
import viettel.dac.backend.template.dto.TemplateResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/plugins")
@RequiredArgsConstructor
@Tag(name = "Plugins", description = "API for plugin management and template creation via plugins")
public class PluginController {

    private final PluginService pluginService;
    private final PluginTemplateService pluginTemplateService;

    @GetMapping("/types")
    @Operation(
            summary = "Get all plugin types",
            description = "Retrieves all registered plugin types"
    )
    public ResponseEntity<List<String>> getPluginTypes() {
        return ResponseEntity.ok(pluginService.getAllPluginTypes());
    }

    @PostMapping("/templates")
    @Operation(
            summary = "Create a template",
            description = "Creates a template using the specified plugin type"
    )
    public ResponseEntity<TemplateResponseDto> createTemplate(
            @Parameter(description = "Plugin type", required = true) @RequestParam String type,
            @Parameter(description = "Template data", required = true) @Valid @RequestBody Map<String, Object> templateData) {

        // For Phase 1, we'll use a mock user ID
        UUID userId = UUID.randomUUID();

        TemplateResponseDto template = pluginTemplateService.createTemplate(type, templateData, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate template data",
            description = "Validates template data for the specified plugin type"
    )
    public ResponseEntity<Boolean> validateTemplate(
            @Parameter(description = "Plugin type", required = true) @RequestParam String type,
            @Parameter(description = "Template data", required = true) @RequestBody Map<String, Object> templateData) {

        boolean valid = pluginService.validateTemplate(type, templateData);
        return ResponseEntity.ok(valid);
    }
}