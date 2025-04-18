package viettel.dac.backend.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import viettel.dac.backend.security.entity.Role;
import viettel.dac.backend.security.enums.RoleType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleType name);
}