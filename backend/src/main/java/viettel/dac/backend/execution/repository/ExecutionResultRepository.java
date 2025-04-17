package viettel.dac.backend.execution.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.execution.entity.ExecutionResult;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ExecutionResult entities.
 */
@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, UUID> {

    Page<ExecutionResult> findByTemplateId(UUID templateId, Pageable pageable);
    Page<ExecutionResult> findByUserId(UUID userId, Pageable pageable);
    Page<ExecutionResult> findByStatus(ExecutionStatus status, Pageable pageable);
    Page<ExecutionResult> findByTemplateIdAndUserId(UUID templateId, UUID userId, Pageable pageable);
    Page<ExecutionResult> findByTemplateIdAndStatus(UUID templateId, ExecutionStatus status, Pageable pageable);
    Page<ExecutionResult> findByUserIdAndStatus(UUID userId, ExecutionStatus status, Pageable pageable);
    @Query("SELECT e FROM ExecutionResult e WHERE " +
            "(:templateId IS NULL OR e.templateId = :templateId) AND " +
            "(:userId IS NULL OR e.userId = :userId) AND " +
            "(:status IS NULL OR e.status = :status)")
    Page<ExecutionResult> findByFilters(
            @Param("templateId") UUID templateId,
            @Param("userId") UUID userId,
            @Param("status") ExecutionStatus status,
            Pageable pageable);
    List<ExecutionResult> findByStatusAndStartTimeBefore(ExecutionStatus status, Instant beforeTime);
    List<ExecutionResult> findByEndTimeBefore(Instant beforeTime);
    void deleteByTemplateId(UUID templateId);
    void deleteByEndTimeBefore(Instant beforeTime);
}
