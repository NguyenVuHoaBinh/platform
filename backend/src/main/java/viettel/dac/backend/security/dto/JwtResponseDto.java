package viettel.dac.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {

    private String token;
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
}