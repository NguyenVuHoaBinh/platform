package viettel.dac.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.common.exception.ResourceNotFoundException;
import viettel.dac.backend.security.dto.PasswordChangeRequestDto;
import viettel.dac.backend.security.dto.UserRequestDto;
import viettel.dac.backend.security.dto.UserResponseDto;
import viettel.dac.backend.security.entity.Role;
import viettel.dac.backend.security.entity.User;
import viettel.dac.backend.security.enums.RoleType;
import viettel.dac.backend.security.mapper.UserMapper;
import viettel.dac.backend.security.repository.RoleRepository;
import viettel.dac.backend.security.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(UUID id, UserRequestDto userRequest, UUID currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if the current user is the owner or an admin
        boolean isAdmin = hasAdminRole(currentUserId);
        if (!user.getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You don't have permission to update this user");
        }

        // Update user fields using our mapper
        // Instead of calling updateUserFromDto, we now use updateUserRoles which handles roles properly
        userMapper.updateUserRoles(userRequest, user);

        // Only admins can update roles directly
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty() && !isAdmin) {
            throw new AccessDeniedException("Only administrators can update user roles");
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deactivateUser(UUID id, UUID currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Only admins can deactivate users (or users themselves)
        if (!user.getId().equals(currentUserId) && !hasAdminRole(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to deactivate this user");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(UUID id, UUID currentUserId) {
        // Only admins can activate users
        if (!hasAdminRole(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to activate users");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequestDto passwordChangeRequest, UUID currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Only the user themselves or admins can change passwords
        if (!user.getId().equals(currentUserId) && !hasAdminRole(currentUserId)) {
            throw new AccessDeniedException("You don't have permission to change this user's password");
        }

        // If it's the user changing their own password, verify the old password
        if (user.getId().equals(currentUserId)) {
            if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        // Change the password
        user.setPasswordHash(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        userRepository.save(user);
    }

    private boolean hasAdminRole(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleType.ROLE_ADMIN))
                .orElse(false);
    }
}