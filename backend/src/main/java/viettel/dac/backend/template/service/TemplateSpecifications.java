package viettel.dac.backend.template.service;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.entity.TemplateTag;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.Set;
import java.util.UUID;

/**
 * Specifications for Template queries to support dynamic filtering.
 */
public class TemplateSpecifications {

    private TemplateSpecifications() {
        // Private constructor to prevent instantiation
    }

    public static Specification<BaseTemplate> hasNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<BaseTemplate> hasType(TemplateType type) {
        return (root, query, cb) -> cb.equal(root.get("templateType"), type);
    }

    public static Specification<BaseTemplate> hasCreator(UUID creatorId) {
        return (root, query, cb) -> cb.equal(root.get("createdBy"), creatorId);
    }

    public static Specification<BaseTemplate> hasTags(Set<String> tagNames, long tagCount) {
        return (root, query, cb) -> {
            // We need a subquery to count the number of matching tags
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                // Distinct to avoid duplicates
                query.distinct(true);

                // Create subquery to count tags
                Subquery<Long> subquery = query.subquery(Long.class);
                var subRoot = subquery.from(BaseTemplate.class);
                Join<BaseTemplate, TemplateTag> tagJoin = subRoot.join("tags", JoinType.INNER);

                subquery.select(cb.count(tagJoin))
                        .where(
                                cb.equal(subRoot, root),
                                tagJoin.get("tagName").in(tagNames)
                        );

                // Match only if the template has all the requested tags
                return cb.equal(subquery, tagCount);
            }
            return null;
        };
    }

    public static Specification<BaseTemplate> isActive() {
        return (root, query, cb) -> cb.equal(root.get("active"), true);
    }
}
