package viettel.dac.backend.execution.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.common.domain.BaseEntity;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing the result of a template execution.
 * Stores the execution outcome, status, timing, and other metrics.
 */
@Entity
@Table(name = "execution_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    private Object result;

    @Column(name = "error_message")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics", columnDefinition = "jsonb")
    private Map<String, Object> metrics;

    /**
     * Calculate the duration of the execution in milliseconds.
     * Returns null if either start time or end time is not set.
     *
     * @return The duration in milliseconds, or null if not calculable
     */
    @Transient
    public Long getDurationMs() {
        if (startTime != null && endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return null;
    }

    /**
     * Mark this execution as running.
     * Sets the status to RUNNING and records the start time.
     */
    public void markAsRunning() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = Instant.now();
    }

    /**
     * Mark this execution as completed.
     * Sets the status to COMPLETED and records the end time.
     *
     * @param result The result of the execution
     */
    public void markAsCompleted(Object result) {
        this.status = ExecutionStatus.COMPLETED;
        this.endTime = Instant.now();
        this.result = result;
    }

    /**
     * Mark this execution as failed.
     * Sets the status to FAILED, records the end time, and stores the error message.
     *
     * @param errorMessage The error message explaining the failure
     */
    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.endTime = Instant.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Mark this execution as timed out.
     * Sets the status to TIMEOUT and records the end time.
     */
    public void markAsTimedOut() {
        this.status = ExecutionStatus.TIMEOUT;
        this.endTime = Instant.now();
        this.errorMessage = "Execution timed out";
    }

    /**
     * Mark this execution as cancelled.
     * Sets the status to CANCELLED and records the end time.
     */
    public void markAsCancelled() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = Instant.now();
        this.errorMessage = "Execution cancelled by user";
    }
}
