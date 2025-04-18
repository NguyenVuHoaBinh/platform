package viettel.dac.backend.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.template.entity.ApiTemplate;
import viettel.dac.backend.template.enums.HttpMethod;


import java.util.List;
import java.util.UUID;

@Repository
public interface ApiTemplateRepository extends JpaRepository<ApiTemplate, UUID> {

    List<ApiTemplate> findByEndpointContainingIgnoreCase(String endpointSubstring);

    List<ApiTemplate> findByHttpMethod(HttpMethod httpMethod);

    List<ApiTemplate> findByContentTypeContainingIgnoreCase(String contentType);

    @Query("SELECT a FROM ApiTemplate a WHERE LOWER(a.endpoint) LIKE CONCAT('%', LOWER(:domain), '%')")
    List<ApiTemplate> findByDomain(@Param("domain") String domain);
}