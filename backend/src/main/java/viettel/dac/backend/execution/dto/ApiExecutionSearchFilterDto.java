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
public class ApiExecutionSearchFilterDto {

    // Base execution filters
    private UUID templateId;
    private UUID userId;
    private ExecutionStatus status;
    private Instant startTimeFrom;
    private Instant startTimeTo;
    private Instant endTimeFrom;
    private Instant endTimeTo;

    // API-specific filters
    private Integer statusCode;
    private Boolean successful;
    private Long maxResponseTimeMs;
}
