package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseExecutionDto {
    private UUID id;
    private UUID templateId;
    private String templateName;
    private UUID userId;
    private ExecutionStatus status;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private String errorMessage;
    private Map<String, Object> metrics;
}