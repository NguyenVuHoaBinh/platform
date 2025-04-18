package viettel.dac.backend.execution.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.execution.entity.ApiExecution;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiExecutionRepository extends JpaRepository<ApiExecution, UUID>,
        JpaSpecificationExecutor<ApiExecution> {

    List<ApiExecution> findByStatusCode(Integer statusCode);

    List<ApiExecution> findBySuccessful(Boolean successful);

    List<ApiExecution> findByResponseTimeMsGreaterThan(Long thresholdMs);

    @Query("SELECT a FROM ApiExecution a WHERE a.templateId = :templateId")
    Page<ApiExecution> findByTemplateId(@Param("templateId") UUID templateId, Pageable pageable);
}
