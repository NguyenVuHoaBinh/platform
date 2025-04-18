package viettel.dac.backend.security.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import viettel.dac.backend.security.model.UserDetailsImpl;

import java.util.UUID;

/**
 * Aspect to automatically inject the current user's ID into service methods
 * that have a 'userId' parameter.
 */
@Aspect
@Component
public class CurrentUserAspect {

    @Around("execution(* viettel.dac.backend..service.*.*(.., java.util.UUID, ..)) && args(.., userId, ..)")
    public Object injectCurrentUserId(ProceedingJoinPoint joinPoint, UUID userId) throws Throwable {
        // If the userId is null, get it from the authenticated user
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                userId = userDetails.getId();
            }
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }
}