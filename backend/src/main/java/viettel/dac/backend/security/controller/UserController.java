package viettel.dac.backend.security.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import viettel.dac.backend.security.dto.PasswordChangeRequestDto;
import viettel.dac.backend.security.dto.UserRequestDto;
import viettel.dac.backend.security.dto.UserResponseDto;
import viettel.dac.backend.security.model.UserDetailsImpl;
import viettel.dac.backend.security.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for user management")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users with pagination (Admin only)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by ID (Admin can access any user, users can only access themselves)"
    )
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Retrieves the currently authenticated user's details"
    )
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates a user's details (Admin can update any user, users can only update themselves)"
    )
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequestDto userRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserResponseDto updatedUser = userService.updateUser(id, userRequest, userDetails.getId());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate user",
            description = "Deactivates a user account (Admin can deactivate any user, users can deactivate themselves)"
    )
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userService.deactivateUser(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(
            summary = "Activate user",
            description = "Activates a deactivated user account (Admin only)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userService.activateUser(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    @Operation(
            summary = "Change password",
            description = "Changes a user's password (Admin can change any user's password, users can only change their own)"
    )
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userService.changePassword(id, passwordChangeRequest, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}