package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "template_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"template_id", "tag_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ToolTemplate template;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateTag that = (TemplateTag) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        if (template == null || that.template == null) return false;
        if (tagName == null || that.tagName == null) return false;

        return template.getId().equals(that.template.getId()) &&
                tagName.equals(that.tagName);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }

        int result = template != null && template.getId() != null ? template.getId().hashCode() : 0;
        result = 31 * result + (tagName != null ? tagName.hashCode() : 0);
        return result;
    }
}