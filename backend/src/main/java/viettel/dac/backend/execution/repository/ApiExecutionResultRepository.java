package viettel.dac.backend.execution.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.execution.entity.ApiExecutionResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiExecutionResultRepository extends JpaRepository<ApiExecutionResult, UUID> {

    List<ApiExecutionResult> findByStatusCode(Integer statusCode);

    List<ApiExecutionResult> findBySuccessful(Boolean successful);

    List<ApiExecutionResult> findByResponseTimeMsGreaterThan(Long thresholdMs);

    @Query("SELECT a FROM ApiExecutionResult a JOIN FETCH a.executionResult e WHERE a.id = :id")
    Optional<ApiExecutionResult> findByIdWithExecutionResult(@Param("id") UUID id);

    @Query("SELECT a FROM ApiExecutionResult a JOIN a.executionResult e WHERE e.templateId = :templateId")
    Page<ApiExecutionResult> findByTemplateId(@Param("templateId") UUID templateId, Pageable pageable);
}