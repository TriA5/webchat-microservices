package user_service.example.user_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserRegisterDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    @Email
    private String email;

    private String phoneNumber;
    private String dateOfBirth; // yyyy-MM-dd
    private Boolean gender;
    private String firstName;
    private String lastName;

    // getters và setters
}

