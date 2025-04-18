package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.template.enums.HttpMethod;
import viettel.dac.backend.template.enums.TemplateType;


import java.util.Map;

@Entity
@Table(name = "api_templates")
@DiscriminatorValue("API")
@PrimaryKeyJoinColumn(name = "template_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApiTemplate extends BaseTemplate {

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

    @PrePersist
    public void prePersist() {
        if (this.getTemplateType() == null) {
            this.setTemplateType(TemplateType.API);
        }
    }
}
