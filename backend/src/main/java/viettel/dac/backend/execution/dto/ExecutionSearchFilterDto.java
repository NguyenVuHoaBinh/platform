package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionSearchFilterDto {

    private UUID templateId;
    private UUID userId;
    private ExecutionStatus status;
    private Instant startTimeFrom;
    private Instant startTimeTo;
    private Instant endTimeFrom;
    private Instant endTimeTo;
}
