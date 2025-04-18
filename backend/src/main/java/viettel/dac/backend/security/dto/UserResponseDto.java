package viettel.dac.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private Set<String> roles;
    private Instant createdAt;
    private Instant lastModifiedAt;
}