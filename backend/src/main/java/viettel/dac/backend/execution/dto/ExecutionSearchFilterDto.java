package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;  // Change this from Builder to SuperBuilder
import viettel.dac.backend.execution.enums.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder  // Change from @Builder to @SuperBuilder
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
