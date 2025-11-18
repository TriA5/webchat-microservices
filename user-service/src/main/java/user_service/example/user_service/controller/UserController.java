package user_service.example.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.AuthenticationException;
import jakarta.mail.MessagingException;
import user_service.example.user_service.dto.UserProfileDTO;
import user_service.example.user_service.dto.UserRegisterDTO;
import user_service.example.user_service.entity.User;
import user_service.example.user_service.repository.UserRepository;
import user_service.example.user_service.security.JwtResponse;
import user_service.example.user_service.security.LoginRequest;
import user_service.example.user_service.service.JWT.JwtService;
import user_service.example.user_service.service.user.UserServiceImp;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/users")
public class UserController { 
    //
    @Autowired
    private UserServiceImp userServiceImp;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    //
    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service!";
    }
    // đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody UserRegisterDTO dto) throws MessagingException {
        return userServiceImp.register(dto);
    }


    @GetMapping("/active-account")
    public ResponseEntity<?> activeAccount(@RequestParam String email, @RequestParam String activationCode) {
        return userServiceImp.activeAccount(email, activationCode);
    }

    // đăng nhập
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                // Lấy user từ DB
                User user = userRepository.findByUsername(loginRequest.getUsername());
                if (user == null) {
                    throw new RuntimeException("Không tìm thấy user");
                }

                // Kiểm tra trạng thái kích hoạt
                if (!user.isEnabled()) {
                    return ResponseEntity
                            .badRequest()
                            .body("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để xác thực!");
                }
                 // Kiểm tra trạng thái block
                if (!user.isStatus()) {
                    return ResponseEntity
                            .badRequest()
                            .body("Tài khoản đã bị khoá. Vui lòng liên hệ quản trị viên để biết thêm chi tiết!");
                }

                // Nếu OK thì cấp JWT
                final String jwtToken = jwtService.generateToken(loginRequest.getUsername());
                return ResponseEntity.ok(new JwtResponse(jwtToken));
            }

        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        return ResponseEntity.badRequest().body("Xác thực không thành công");
    }
    //thay đổi ảnh đại diện
    @PutMapping("/change-avatar")
    public ResponseEntity<?> changeAvatar(@RequestBody JsonNode jsonData) {
        try{
            return userServiceImp.changeAvatar(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //Update Profile
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDTO dto) {
        return userServiceImp.updateProfile(dto);
    }
    //Quên mật khẩu
    @PutMapping(path = "/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody JsonNode jsonNode) {
        try{
            return userServiceImp.forgotPassword(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //Chage Password
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody JsonNode jsonData) {
        System.out.println(jsonData);
        try{
            return userServiceImp.changePassword(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    //Get user by ID (for displaying names in group chat)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        return userServiceImp.getUserById(userId);
    }

    // Search user by phone (for other services)
    @GetMapping("/search")
    public ResponseEntity<?> searchByPhone(@RequestParam String phone) {
        try {
            return userServiceImp.searchByPhone(phone);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //block user
    @PutMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(@PathVariable UUID userId) {
        try{
            return userServiceImp.blockUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //unblock user
    @PutMapping("/unblock/{userId}")
    public ResponseEntity<?> unblockUser(@PathVariable UUID userId) {
        try{
            return userServiceImp.unblockUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //tổng số user
    @GetMapping("/total-users")
    public ResponseEntity<?> countTotalUsers() {
        try{
            Long totalUsers = userRepository.countUser();
            return ResponseEntity.ok(totalUsers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //số user đang active
    @GetMapping("/active-users")
    public ResponseEntity<?> countActiveUsers() {
        try{
            Long activeUsers = userRepository.countStatusUsers();
            return ResponseEntity.ok(activeUsers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //Tổng lượng user tăng trưởng theo tháng
}