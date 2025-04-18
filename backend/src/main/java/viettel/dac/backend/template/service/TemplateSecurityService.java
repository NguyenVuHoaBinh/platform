package viettel.dac.backend.template.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import viettel.dac.backend.template.repository.TemplateRepository;

import java.util.UUID;

/**
 * Service for template security-related operations.
 */
@Service
@RequiredArgsConstructor
public class TemplateSecurityService {

    private final TemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public boolean isTemplateCreator(UUID templateId, UUID userId) {
        return templateRepository.findById(templateId)
                .map(template -> template.getCreatedBy() != null &&
                        template.getCreatedBy().equals(userId))
                .orElse(false);
    }
}
