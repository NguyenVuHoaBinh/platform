package viettel.dac.backend.execution.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends JpaRepository<BaseExecution, UUID>,
        JpaSpecificationExecutor<BaseExecution> {

    Page<BaseExecution> findByTemplateId(UUID templateId, Pageable pageable);

    Page<BaseExecution> findByUserId(UUID userId, Pageable pageable);

    Page<BaseExecution> findByStatus(ExecutionStatus status, Pageable pageable);

    Page<BaseExecution> findByTemplateIdAndUserId(UUID templateId, UUID userId, Pageable pageable);

    Page<BaseExecution> findByTemplateIdAndStatus(UUID templateId, ExecutionStatus status, Pageable pageable);

    Page<BaseExecution> findByUserIdAndStatus(UUID userId, ExecutionStatus status, Pageable pageable);

    @Query("SELECT e FROM BaseExecution e WHERE " +
            "(:templateId IS NULL OR e.templateId = :templateId) AND " +
            "(:userId IS NULL OR e.userId = :userId) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:startTimeFrom IS NULL OR e.startTime >= :startTimeFrom) AND " +
            "(:startTimeTo IS NULL OR e.startTime <= :startTimeTo) AND " +
            "(:endTimeFrom IS NULL OR e.endTime >= :endTimeFrom) AND " +
            "(:endTimeTo IS NULL OR e.endTime <= :endTimeTo)")
    Page<BaseExecution> findByFilters(
            @Param("templateId") UUID templateId,
            @Param("userId") UUID userId,
            @Param("status") ExecutionStatus status,
            @Param("startTimeFrom") Instant startTimeFrom,
            @Param("startTimeTo") Instant startTimeTo,
            @Param("endTimeFrom") Instant endTimeFrom,
            @Param("endTimeTo") Instant endTimeTo,
            Pageable pageable);

    List<BaseExecution> findByStatusAndStartTimeBefore(ExecutionStatus status, Instant beforeTime);

    List<BaseExecution> findByEndTimeBefore(Instant beforeTime);

    void deleteByTemplateId(UUID templateId);

    void deleteByEndTimeBefore(Instant beforeTime);
}