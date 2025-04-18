package viettel.dac.backend.execution.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "api_executions")
@DiscriminatorValue("API")
@PrimaryKeyJoinColumn(name = "execution_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiExecution extends BaseExecution {

    @Column(name = "status_code")
    private Integer statusCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_headers", columnDefinition = "jsonb")
    private Map<String, String> responseHeaders;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private Object responseBody;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "successful")
    private Boolean successful;

    @PrePersist
    public void prePersist() {
        if (this.getExecutionType() == null) {
            this.setExecutionType("API");
        }
    }
}
