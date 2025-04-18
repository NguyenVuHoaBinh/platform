package viettel.dac.backend.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    private Set<String> roles;

    private Boolean active;
}