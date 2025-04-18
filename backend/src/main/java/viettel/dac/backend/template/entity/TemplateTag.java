package viettel.dac.backend.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(of = {"template.id", "tagName"})
public class TemplateTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private BaseTemplate template;

    @Column(name = "tag_name", nullable = false)
    private String tagName;
}
