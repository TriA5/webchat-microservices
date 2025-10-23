package friendship_service.example.friendship_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID idUser;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean gender;
    private LocalDate dateOfBirth;
    private String avatar;
}
