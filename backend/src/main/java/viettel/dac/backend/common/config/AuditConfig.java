package viettel.dac.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Provides the current auditor (user) for JPA auditing.
     * In Phase 1, we use a mock user ID. In later phases, this will be replaced
     * with authentication-based user identification.
     *
     * @return AuditorAware implementation
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        // For Phase 1, use a constant mock user ID
        // Later this will be integrated with authentication
        return () -> Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
