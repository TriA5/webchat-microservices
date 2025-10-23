package chat_service.example.chat_service.client;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID idUser;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String gender;
    private String dateOfBirth;
    private String avatar;
}
