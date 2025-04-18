package viettel.dac.backend.execution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.execution.repository.ExecutionResultRepository;
import viettel.dac.backend.security.enums.RoleType;

import java.util.UUID;

/**
 * Service for execution security-related operations.
 */
@Service
@RequiredArgsConstructor
public class ExecutionSecurityService {

    private final ExecutionResultRepository executionResultRepository;

    @Transactional(readOnly = true)
    public boolean canAccessExecution(UUID executionId, UUID userId) {
        // Check if user is an admin
        if (hasAdminRole()) {
            return true;
        }

        // Check if the user is the owner of the execution
        return executionResultRepository.findById(executionId)
                .map(execution -> execution.getUserId() != null &&
                        execution.getUserId().equals(userId))
                .orElse(false);
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> authority.equals(RoleType.ROLE_ADMIN.name()));
        }

        return false;
    }
}