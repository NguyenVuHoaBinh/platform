package viettel.dac.backend.execution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequestDto {

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    private Map<String, Object> parameters;

    private Integer timeout;
}