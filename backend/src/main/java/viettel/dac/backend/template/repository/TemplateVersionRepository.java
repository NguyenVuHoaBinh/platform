package viettel.dac.backend.template.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.TemplateVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TemplateVersion entities.
 */
@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, UUID> {

    List<TemplateVersion> findByTemplateIdOrderByVersionDesc(UUID templateId);
    Optional<TemplateVersion> findByTemplateIdAndVersion(UUID templateId, String version);
    @Query("SELECT v FROM TemplateVersion v WHERE v.template.id = :templateId ORDER BY v.createdAt DESC")
    List<TemplateVersion> findLatestVersionByTemplateId(@Param("templateId") UUID templateId, Pageable pageable);
    Page<TemplateVersion> findByCreatedBy(UUID createdBy, Pageable pageable);
    boolean existsByTemplateIdAndVersion(UUID templateId, String version);
    void deleteByTemplateId(UUID templateId);
}