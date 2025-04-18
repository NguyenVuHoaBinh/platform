package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.template.enums.HttpMethod;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "api_tool_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiToolTemplate {

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private ToolTemplate toolTemplate;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false)
    private HttpMethod httpMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, String> headers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_params", columnDefinition = "jsonb")
    private Map<String, Object> queryParams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_body", columnDefinition = "jsonb")
    private Object requestBody;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "follow_redirects")
    private Boolean followRedirects = true;
}
