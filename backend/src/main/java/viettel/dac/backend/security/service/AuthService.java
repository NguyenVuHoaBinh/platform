package viettel.dac.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceAlreadyExistsException;
import viettel.dac.backend.security.dto.JwtResponseDto;
import viettel.dac.backend.security.dto.LoginRequestDto;
import viettel.dac.backend.security.dto.SignupRequestDto;
import viettel.dac.backend.security.entity.Role;
import viettel.dac.backend.security.entity.User;
import viettel.dac.backend.security.enums.RoleType;
import viettel.dac.backend.security.model.UserDetailsImpl;
import viettel.dac.backend.security.repository.RoleRepository;
import viettel.dac.backend.security.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtService.generateJwtToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return JwtResponseDto.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .roles(roles)
                .build();
    }

    @Transactional
    public void registerUser(SignupRequestDto signupRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }

        // Check if email exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already in use");
        }

        // Create new user
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signupRequest.getPassword()))
                .fullName(signupRequest.getFullName())
                .active(true)
                .build();

        Set<Role> roles = new HashSet<>();

        // If no roles are specified, assign the default ROLE_USER
        if (signupRequest.getRoles() == null || signupRequest.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            roles.add(userRole);
        } else {
            // Assign specified roles
            signupRequest.getRoles().forEach(role -> {
                try {
                    RoleType roleType = RoleType.valueOf(role);
                    Role foundRole = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                    roles.add(foundRole);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Error: Invalid role name: " + role);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
    }
}