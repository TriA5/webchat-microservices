package user_service.example.user_service.service.user;


import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import user_service.example.user_service.dto.UserProfileDTO;
import user_service.example.user_service.dto.UserRegisterDTO;


public interface UserService {
    public ResponseEntity<?> register(UserRegisterDTO dto);
    //Thay đổi ảnh đại diện
    public ResponseEntity<?> changeAvatar(JsonNode userJson);
    //Update Profile
    public ResponseEntity<?> updateProfile(UserProfileDTO dto);
    //Update password
    public ResponseEntity<?> forgotPassword(JsonNode jsonNode);


    public ResponseEntity<?> changePassword(JsonNode userJson);

   
}

