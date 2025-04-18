package viettel.dac.backend.execution.service.specification;

import org.springframework.data.jpa.domain.Specification;
import viettel.dac.backend.execution.entity.ApiExecution;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public class ApiExecutionSpecifications {

    private ApiExecutionSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<ApiExecution> hasTemplateId(UUID templateId) {
        return (root, query, cb) -> cb.equal(root.get("templateId"), templateId);
    }

    public static Specification<ApiExecution> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<ApiExecution> hasStatus(ExecutionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ApiExecution> hasStatusCode(Integer statusCode) {
        return (root, query, cb) -> cb.equal(root.get("statusCode"), statusCode);
    }

    public static Specification<ApiExecution> isSuccessful(Boolean successful) {
        return (root, query, cb) -> cb.equal(root.get("successful"), successful);
    }

    public static Specification<ApiExecution> responseTimeLessThan(Long maxTimeMs) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("responseTimeMs"), maxTimeMs);
    }

    public static Specification<ApiExecution> responseTimeGreaterThan(Long minTimeMs) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("responseTimeMs"), minTimeMs);
    }

    public static Specification<ApiExecution> startTimeAfter(Instant time) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), time);
    }

    public static Specification<ApiExecution> startTimeBefore(Instant time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), time);
    }

    public static Specification<ApiExecution> endTimeAfter(Instant time) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endTime"), time);
    }

    public static Specification<ApiExecution> endTimeBefore(Instant time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), time);
    }
}