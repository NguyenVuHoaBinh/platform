package viettel.dac.backend.security.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import viettel.dac.backend.security.dto.UserRequestDto;
import viettel.dac.backend.security.dto.UserResponseDto;
import viettel.dac.backend.security.entity.Role;
import viettel.dac.backend.security.entity.User;
import viettel.dac.backend.security.enums.RoleType;
import viettel.dac.backend.security.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    @Autowired
    protected RoleRepository roleRepository;

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringSet")
    public abstract UserResponseDto toDto(User user);

    @Named("rolesToStringSet")
    protected Set<String> rolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true) // We'll handle roles separately
    public abstract void updateUserFromDto(UserRequestDto dto, @MappingTarget User user);

    /**
     * Manually map roles from DTO to entity
     */
    public void updateUserRoles(UserRequestDto dto, @MappingTarget User user) {
        // First apply the standard mapping
        updateUserFromDto(dto, user);

        // Then handle the roles if they're present
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();

            for (String roleName : dto.getRoles()) {
                try {
                    RoleType roleType = RoleType.valueOf(roleName);
                    roleRepository.findByName(roleType)
                            .ifPresent(roles::add);
                } catch (IllegalArgumentException e) {
                    // Handle invalid role name
                    // Log or throw an exception as needed
                }
            }

            // Only set if we found valid roles
            if (!roles.isEmpty()) {
                user.setRoles(roles);
            }
        }
    }
}