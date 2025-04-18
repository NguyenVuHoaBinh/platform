package viettel.dac.backend.execution.dto;

import viettel.dac.backend.execution.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionResultResponseDto {

    // Base execution result fields
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

    // API-specific fields
    private Integer statusCode;
    private Map<String, String> responseHeaders;
    private Object responseBody;
    private Long responseTimeMs;
    private Boolean successful;
}
