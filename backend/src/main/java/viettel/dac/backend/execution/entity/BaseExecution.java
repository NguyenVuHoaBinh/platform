package viettel.dac.backend.execution.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.common.domain.BaseEntity;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "executions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "execution_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseExecution extends BaseEntity {

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

    @Column(name = "execution_type", insertable = false, updatable = false)
    private String executionType;

    @Transient
    public Long getDurationMs() {
        if (startTime != null && endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return null;
    }

    public void markAsRunning() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = Instant.now();
    }

    public void markAsCompleted(Object result) {
        this.status = ExecutionStatus.COMPLETED;
        this.endTime = Instant.now();
        this.result = result;
    }

    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.endTime = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void markAsTimedOut() {
        this.status = ExecutionStatus.TIMEOUT;
        this.endTime = Instant.now();
        this.errorMessage = "Execution timed out";
    }

    public void markAsCancelled() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = Instant.now();
        this.errorMessage = "Execution cancelled by user";
    }
}