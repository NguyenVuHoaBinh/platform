package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.common.domain.BaseEntity;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tool_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "version", nullable = false)
    private String version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties", columnDefinition = "jsonb")
    private Map<String, Object> properties;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private TemplateType templateType;

    @Column(name = "active")
    private boolean active = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TemplateTag> tags = new HashSet<>();

    public TemplateTag addTag(String tagName) {
        TemplateTag tag = new TemplateTag();
        tag.setTemplate(this);
        tag.setTagName(tagName);
        this.tags.add(tag);
        return tag;
    }

    public boolean removeTag(String tagName) {
        return this.tags.removeIf(tag -> tag.getTagName().equals(tagName));
    }
}