package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import viettel.dac.backend.common.domain.BaseEntity;
import viettel.dac.backend.template.enums.TemplateType;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "templates")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "template_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseTemplate extends BaseEntity {

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
    @Column(name = "template_type", insertable = false, updatable = false)
    private TemplateType templateType;

    @Column(name = "active")
    private boolean active = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TemplateTag> tags = new HashSet<>();

    public void addTag(String tagName) {
        TemplateTag tag = new TemplateTag();
        tag.setTemplate(this);
        tag.setTagName(tagName);
        this.tags.add(tag);
    }

    public boolean removeTag(String tagName) {
        return this.tags.removeIf(tag -> tag.getTagName().equals(tagName));
    }
}