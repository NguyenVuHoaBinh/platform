package viettel.dac.backend.template.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.ToolTemplate;
import viettel.dac.backend.template.enums.TemplateType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ToolTemplateRepository extends JpaRepository<ToolTemplate, UUID> {

    Page<ToolTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<ToolTemplate> findByTemplateType(TemplateType templateType, Pageable pageable);
    Page<ToolTemplate> findByCreatedBy(UUID createdBy, Pageable pageable);
    Page<ToolTemplate> findByActiveTrue(Pageable pageable);
    Optional<ToolTemplate> findByIdAndActiveTrue(UUID id);
    @Query("SELECT t FROM ToolTemplate t JOIN t.tags tag WHERE tag.tagName = :tagName")
    Page<ToolTemplate> findByTagName(@Param("tagName") String tagName, Pageable pageable);
    @Query("SELECT t FROM ToolTemplate t JOIN t.tags tag WHERE tag.tagName IN :tagNames GROUP BY t HAVING COUNT(DISTINCT tag.tagName) = :tagCount")
    Page<ToolTemplate> findByTagNames(@Param("tagNames") List<String> tagNames, @Param("tagCount") long tagCount, Pageable pageable);
    @Query("SELECT DISTINCT t FROM ToolTemplate t LEFT JOIN t.tags tag WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:templateType IS NULL OR t.templateType = :templateType) AND " +
            "(:createdBy IS NULL OR t.createdBy = :createdBy) AND " +
            "(:tagName IS NULL OR tag.tagName = :tagName) AND " +
            "t.active = true")
    Page<ToolTemplate> findByFilters(
            @Param("name") String name,
            @Param("templateType") TemplateType templateType,
            @Param("createdBy") UUID createdBy,
            @Param("tagName") String tagName,
            Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}