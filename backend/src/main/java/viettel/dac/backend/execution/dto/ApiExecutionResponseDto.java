package viettel.dac.backend.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionResponseDto extends BaseExecutionDto {
    private Integer statusCode;
    private Map<String, String> responseHeaders;
    private Object responseBody;
    private Long responseTimeMs;
    private Boolean successful;
}
