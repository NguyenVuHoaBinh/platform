package viettel.dac.backend.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.ApiToolTemplate;
import viettel.dac.backend.template.enums.HttpMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ApiToolTemplateRepository extends JpaRepository<ApiToolTemplate, UUID> {

    List<ApiToolTemplate> findByEndpointContainingIgnoreCase(String endpointSubstring);

    List<ApiToolTemplate> findByHttpMethod(HttpMethod httpMethod);

    List<ApiToolTemplate> findByContentTypeContainingIgnoreCase(String contentType);

    @Query("SELECT a FROM ApiToolTemplate a WHERE LOWER(a.endpoint) LIKE CONCAT('%', LOWER(:domain), '%')")
    List<ApiToolTemplate> findByDomain(@Param("domain") String domain);

    @Query("SELECT a FROM ApiToolTemplate a JOIN FETCH a.toolTemplate t WHERE a.id = :id")
    Optional<ApiToolTemplate> findByIdWithToolTemplate(@Param("id") UUID id);
}