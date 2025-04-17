package viettel.dac.backend.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.TemplateTag;

import java.util.List;
import java.util.UUID;


@Repository
public interface TemplateTagRepository extends JpaRepository<TemplateTag, UUID> {

    List<TemplateTag> findByTemplateId(UUID templateId);
    void deleteByTemplateId(UUID templateId);

    @Query("SELECT DISTINCT t.tagName FROM TemplateTag t ORDER BY t.tagName")
    List<String> findAllDistinctTagNames();
    long countByTagName(String tagName);

    @Query("SELECT t.template.id FROM TemplateTag t WHERE t.tagName = :tagName")
    List<UUID> findTemplateIdsByTagName(String tagName);
}