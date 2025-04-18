package viettel.dac.backend.execution.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = "api_execution_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiExecutionResult {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private ExecutionResult executionResult;

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
}
