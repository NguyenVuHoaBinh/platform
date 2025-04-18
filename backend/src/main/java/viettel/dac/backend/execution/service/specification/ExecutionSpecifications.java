package viettel.dac.backend.execution.service.specification;

import org.springframework.data.jpa.domain.Specification;
import viettel.dac.backend.execution.entity.BaseExecution;
import viettel.dac.backend.execution.enums.ExecutionStatus;


import java.time.Instant;
import java.util.UUID;

public class ExecutionSpecifications {

    private ExecutionSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<BaseExecution> hasTemplateId(UUID templateId) {
        return (root, query, cb) -> cb.equal(root.get("templateId"), templateId);
    }

    public static Specification<BaseExecution> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<BaseExecution> hasStatus(ExecutionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<BaseExecution> startTimeAfter(Instant time) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), time);
    }

    public static Specification<BaseExecution> startTimeBefore(Instant time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), time);
    }

    public static Specification<BaseExecution> endTimeAfter(Instant time) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endTime"), time);
    }

    public static Specification<BaseExecution> endTimeBefore(Instant time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), time);
    }
}