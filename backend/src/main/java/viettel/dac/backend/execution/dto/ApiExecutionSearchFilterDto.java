package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionSearchFilterDto extends ExecutionSearchFilterDto {
    private Integer statusCode;
    private Boolean successful;
    private Long maxResponseTimeMs;
}