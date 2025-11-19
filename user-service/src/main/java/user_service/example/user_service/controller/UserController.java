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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // Số lượng người dùng tăng trưởng so với tháng trước
    @GetMapping("/user-growth")
    public ResponseEntity<?> getUserGrowth() {
        try {
            YearMonth currentMonth = YearMonth.now();
            YearMonth lastMonth = currentMonth.minusMonths(1);
            
            Long currentMonthCount = userRepository.countUsersByMonth(
                currentMonth.getYear(), 
                currentMonth.getMonthValue()
            );
            Long lastMonthCount = userRepository.countUsersByMonth(
                lastMonth.getYear(), 
                lastMonth.getMonthValue()
            );
            
            long growth = currentMonthCount - lastMonthCount;
            double growthPercentage = lastMonthCount > 0 
                ? ((double) growth / lastMonthCount) * 100 
                : 0;
            
            Map<String, Object> result = new HashMap<>();
            result.put("currentMonth", currentMonthCount);
            result.put("lastMonth", lastMonthCount);
            result.put("growth", growth);
            result.put("growthPercentage", Math.round(growthPercentage * 100.0) / 100.0);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Số lượng người dùng 6 tháng gần đây
    @GetMapping("/users-last-6-months")
    public ResponseEntity<?> getUsersLast6Months() {
        try {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            List<Object[]> rawData = userRepository.countUsersByLast6Months(sixMonthsAgo);
            
            // Tạo map để lưu kết quả theo tháng
            Map<String, Long> monthlyData = new HashMap<>();
            
            // Khởi tạo 6 tháng gần nhất với giá trị 0
            YearMonth current = YearMonth.now();
            for (int i = 0; i < 6; i++) {
                YearMonth month = current.minusMonths(i);
                String key = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                monthlyData.put(key, 0L);
            }
            
            // Điền dữ liệu thực tế
            for (Object[] row : rawData) {
                int year = ((Number) row[0]).intValue();
                int month = ((Number) row[1]).intValue();
                long count = ((Number) row[2]).longValue();
                String key = year + "-" + String.format("%02d", month);
                monthlyData.put(key, count);
            }
            
            // Chuyển sang list có thứ tự
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 5; i >= 0; i--) {
                YearMonth month = current.minusMonths(i);
                String key = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                
                Map<String, Object> item = new HashMap<>();
                item.put("month", key);
                item.put("monthName", "Tháng " + month.getMonthValue());
                item.put("count", monthlyData.get(key));
                result.add(item);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
