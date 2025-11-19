package user_service.example.user_service.service.user;


import com.fasterxml.jackson.databind.JsonNode;


import jakarta.transaction.Transactional;
import user_service.example.user_service.dto.UserProfileDTO;
import user_service.example.user_service.dto.UserRegisterDTO;
import user_service.example.user_service.entity.Notification;
import user_service.example.user_service.entity.Role;
import user_service.example.user_service.entity.User;
import user_service.example.user_service.repository.RoleRepository;
import user_service.example.user_service.repository.UserRepository;
import user_service.example.user_service.security.JwtResponse;
import user_service.example.user_service.service.JWT.JwtService;
import user_service.example.user_service.service.UploadImage.UploadImageService;
import user_service.example.user_service.service.email.EmailService;
import user_service.example.user_service.service.util.Base64ToMultipartFileConverter;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class UserServiceImp implements UserService{

    //
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private JwtService jwtService;
    

    // public UserServiceImp(ObjectMapper objectMapper) {
    //     this.objectMapper = objectMapper;
    // }
    //
    @Override
@Transactional
public ResponseEntity<?> register(UserRegisterDTO dto) {
    if (userRepository.existsByUsername(dto.getUsername())) {
        return ResponseEntity.badRequest().body(new Notification("Username đã tồn tại."));
    }
    if (userRepository.existsByEmail(dto.getEmail())) {
        return ResponseEntity.badRequest().body(new Notification("Email đã tồn tại."));
    }
    if (dto.getPhoneNumber() != null && userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
        return ResponseEntity.badRequest().body(new Notification("Số điện thoại đã tồn tại."));
    }

    User user = new User();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    user.setPhoneNumber(dto.getPhoneNumber());
    user.setGender(dto.getGender());
    user.setFirstName(dto.getFirstName());
    user.setLastName(dto.getLastName());
    if (dto.getDateOfBirth() != null) {
        user.setDateOfBirth(java.time.LocalDate.parse(dto.getDateOfBirth()));
    }
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setAvatar("");
    user.setActivationCode(generateActivationCode());
    user.setEnabled(false);
    user.setStatus(true);
    user.setCreatedAt(java.time.LocalDateTime.now());
    user.setUpdatedAt(java.time.LocalDateTime.now());

    List<Role> roleList = new ArrayList<>();
    roleList.add(roleRepository.findByNameRole("USER"));
    user.setListRoles(roleList);

    userRepository.save(user);
    sendEmailActivation(user.getEmail(), user.getActivationCode());

    return ResponseEntity.ok("Đăng ký thành công!");
}
    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }
    private void sendEmailActivation(String email, String activationCode) {
//        String endpointFE = "https://d451-203-205-27-198.ngrok-free.app";
        // String endpointFE = "http://localhost:3000";
        String endpointFE = "https://unpessimistically-unbewailed-christy.ngrok-free.dev";
        String url = endpointFE + "/active/" + email + "/" + activationCode;
        String subject = "Kích hoạt tài khoản";
        String message = "Cảm ơn bạn đã là thành viên của chúng tôi. Vui lòng kích hoạt tài khoản!: <br/> Mã kích hoạt: <strong>"+ activationCode +"<strong/>";
        message += "<br/> Click vào đây để <a href="+ url +">kích hoạt</a>";
        try {
            emailService.sendMessage("trithuanduong123@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ResponseEntity<?> activeAccount(String email, String activationCode) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(new Notification("Người dùng không tồn tại!"));
        }
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(new Notification("Tài khoản đã được kích hoạt"));
        }
        if (user.getActivationCode().equals(activationCode)) {
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            return ResponseEntity.badRequest().body(new Notification("Mã kích hoạt không chính xác!"));
        }
        return ResponseEntity.ok("Kích hoạt thành công");
    }
    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
    //Thay đổi ảnh đại diện
    @Override
    @Transactional
    public ResponseEntity<?> changeAvatar(JsonNode userJson) {
        try{
            UUID idUser = UUID.fromString(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String dataAvatar = formatStringByJson(String.valueOf(userJson.get("avatar")));

            Optional<User> user = userRepository.findById(idUser);

            // Xoá đi ảnh trước đó trong cloudinary
            if (user.get().getAvatar().length() > 0) {
                uploadImageService.deleteImage(user.get().getAvatar());
            }

            if (Base64ToMultipartFileConverter.isBase64(dataAvatar)) {
                MultipartFile avatarFile = Base64ToMultipartFileConverter.convert(dataAvatar);
                String avatarUrl = uploadImageService.uploadImage(avatarFile, "User_" + idUser);
                user.get().setAvatar(avatarUrl);
            }

            User newUser =  userRepository.save(user.get());
            final String jwtToken = jwtService.generateToken(newUser.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwtToken));

        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    //Update Profile
    @Override
    public ResponseEntity<?> updateProfile(UserProfileDTO dto) {
        try {
            Optional<User> userOpt = userRepository.findById(dto.getIdUser());
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setDateOfBirth(dto.getDateOfBirth());
            if (dto.getGender() != null) {
                user.setGender(dto.getGender());
            }
            // if (dto.getAvatar() != null) {
            //     user.setAvatar(dto.getAvatar());
            // }
            user.setUpdatedAt(java.time.LocalDateTime.now());

            userRepository.save(user);

            return ResponseEntity.ok("Cập nhật thông tin thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Có lỗi xảy ra khi cập nhật thông tin");
        }
    }
    // Quên mật khẩu
    @Override
    public ResponseEntity<?> forgotPassword(JsonNode jsonNode) {
        try{
            User user = userRepository.findByEmail(formatStringByJson(jsonNode.get("email").toString()));

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Đổi mật khẩu cho user
            String passwordTemp = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(passwordTemp));
            userRepository.save(user);

            // Gửi email đê nhận mật khẩu
            sendEmailForgotPassword(user.getEmail(), passwordTemp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    //quên mật khẩu
        // private String generateActivationCode() {
        //     return UUID.randomUUID().toString();
        // }
    private void sendEmailForgotPassword(String email, String password) {
        String subject = "Reset mật khẩu";
        String message = "Mật khẩu tạm thời của bạn là: <strong>" + password + "</strong>";
        message += "<br/> <span>Vui lòng đăng nhập và đổi lại mật khẩu của bạn</span>";
        try {
            emailService.sendMessage("trithuanduong123@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //Charge Password
    @Override
    public ResponseEntity<?> changePassword(JsonNode userJson) {
        try{
            UUID idUser = UUID.fromString(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String newPassword = formatStringByJson(String.valueOf(userJson.get("newPassword")));
            System.out.println(idUser);
            System.out.println(newPassword);
            Optional<User> user = userRepository.findById(idUser);
            user.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user.get());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    private String generateTemporaryPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    public ResponseEntity<?> getUserById(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new Notification("Người dùng không tồn tại"));
        }
        
        User user = userOpt.get();
        user_service.example.user_service.dto.BasicUserDTO dto = new user_service.example.user_service.dto.BasicUserDTO(
            user.getIdUser(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getGender(),
            user.getDateOfBirth(),
            user.getAvatar()
        );
        
        return ResponseEntity.ok(dto);
    }

    // Search user by phone for other services
    public ResponseEntity<?> searchByPhone(String phone) {
        Optional<User> userOpt = userRepository.findByPhoneNumberAndEnabledTrue(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new Notification("Người dùng không tồn tại"));
        }

        User user = userOpt.get();
        user_service.example.user_service.dto.BasicUserDTO dto = new user_service.example.user_service.dto.BasicUserDTO(
            user.getIdUser(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getGender(),
            user.getDateOfBirth(),
            user.getAvatar()
        );

        return ResponseEntity.ok(dto);
    }
    //block user
    @Override
    public ResponseEntity<?> blockUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new Notification("Người dùng không tồn tại"));
        }

        User user = userOpt.get();
        user.setStatus(false);
        userRepository.save(user);

        return ResponseEntity.ok("Người dùng đã bị khoá.");
    }
    //unblock user
    @Override
    public ResponseEntity<?> unblockUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new Notification("Người dùng không tồn tại"));
        }

        User user = userOpt.get();
        user.setStatus(true);
        userRepository.save(user);

        return ResponseEntity.ok("Người dùng đã được mở khoá.");
    }
}

