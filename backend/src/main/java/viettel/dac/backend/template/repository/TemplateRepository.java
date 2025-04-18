package viettel.dac.backend.template.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.BaseTemplate;
import viettel.dac.backend.template.enums.TemplateType;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<BaseTemplate, UUID>,
        JpaSpecificationExecutor<BaseTemplate> {

    Page<BaseTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<BaseTemplate> findByTemplateType(TemplateType templateType, Pageable pageable);

    Page<BaseTemplate> findByCreatedBy(UUID createdBy, Pageable pageable);

    Page<BaseTemplate> findByActiveTrue(Pageable pageable);

    Optional<BaseTemplate> findByIdAndActiveTrue(UUID id);

    @Query("SELECT t FROM BaseTemplate t JOIN t.tags tag WHERE tag.tagName = :tagName")
    Page<BaseTemplate> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT t FROM BaseTemplate t JOIN t.tags tag WHERE tag.tagName IN :tagNames " +
            "GROUP BY t HAVING COUNT(DISTINCT tag.tagName) = :tagCount")
    Page<BaseTemplate> findByTagNames(@Param("tagNames") List<String> tagNames,
                                      @Param("tagCount") long tagCount,
                                      Pageable pageable);

    @Query("SELECT DISTINCT t FROM BaseTemplate t LEFT JOIN t.tags tag WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:templateType IS NULL OR t.templateType = :templateType) AND " +
            "(:createdBy IS NULL OR t.createdBy = :createdBy) AND " +
            "(:tagName IS NULL OR tag.tagName = :tagName) AND " +
            "t.active = true")
    Page<BaseTemplate> findByFilters(
            @Param("name") String name,
            @Param("templateType") TemplateType templateType,
            @Param("createdBy") UUID createdBy,
            @Param("tagName") String tagName,
            Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
