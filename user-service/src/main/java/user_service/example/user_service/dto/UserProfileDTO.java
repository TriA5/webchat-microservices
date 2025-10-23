package user_service.example.user_service.dto;



import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class UserProfileDTO {
    private UUID idUser;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Boolean gender; // true = Nam, false = Nữ
    // private String avatar;
}


