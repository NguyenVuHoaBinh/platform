package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResultResponseDto {

    private UUID id;
    private UUID templateId;
    private String templateName;
    private UUID userId;
    private ExecutionStatus status;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private Object result;
    private String errorMessage;
    private Map<String, Object> metrics;
}