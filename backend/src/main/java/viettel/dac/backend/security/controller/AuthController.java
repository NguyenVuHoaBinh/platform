package viettel.dac.backend.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import viettel.dac.backend.security.dto.JwtResponseDto;
import viettel.dac.backend.security.dto.LoginRequestDto;
import viettel.dac.backend.security.dto.SignupRequestDto;
import viettel.dac.backend.security.service.AuthService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticate a user and return a JWT token"
    )
    public ResponseEntity<JwtResponseDto> authenticateUser(
            @Valid @RequestBody LoginRequestDto loginRequest) {

        JwtResponseDto jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    @Operation(
            summary = "Register user",
            description = "Register a new user account"
    )
    public ResponseEntity<String> registerUser(
            @Valid @RequestBody SignupRequestDto signupRequest) {

        authService.registerUser(signupRequest);
        return ResponseEntity.ok("User registered successfully!");
    }
}