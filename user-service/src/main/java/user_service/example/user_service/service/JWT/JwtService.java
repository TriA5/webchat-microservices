package user_service.example.user_service.service.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import user_service.example.user_service.entity.Role;
import user_service.example.user_service.entity.User;
import user_service.example.user_service.service.UserSecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    private static final String KEY_SECRET = "MTIzNDU2NDU5OThEMzIxM0F6eGMzNTE2NTQzMjEzMjE2NTQ5OHEzMTNhMnMxZDMyMnp4M2MyMQ==";

    @Autowired
    private UserSecurityService userSecurityService;

    // Tạo jwt dựa trên username (tạo thông tin cần trả về cho FE khi đăng nhập thành công)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        User user = userSecurityService.findByUsername(username);
        claims.put("id", user.getIdUser());
        claims.put("avatar", user.getAvatar());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("enabled", user.isEnabled());
        List<Role> roles = user.getListRoles();
        if (roles.size() > 0) {
            for (Role role : roles) {
                if (role.getNameRole().equals("ADMIN")) {
                    claims.put("role", "ADMIN");
                    break;
                }
                if (role.getNameRole().equals("USER")) {
                    claims.put("role", "USER");
                    break;
                }
            }
        }


        return createToken(claims, username);
    }

    // Toạ jwt với các claims đã chọn
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000) ) // Hết hạn sau 30 phút
                .setExpiration(new Date(System.currentTimeMillis() + 100000L * 60 * 60 * 1000) )
                .signWith(SignatureAlgorithm.HS256, getSigneKey())
                .compact();
    }

    // Lấy key_secret
    private Key getSigneKey() {
        byte[] keyByte = Decoders.BASE64.decode(KEY_SECRET);
        return Keys.hmacShaKeyFor(keyByte);
    }

    // Trích xuất thông tin (lấy ra tất cả thông số)
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigneKey()).parseClaimsJws(token).getBody();
    }

    // Trích xuất thông tin cụ thể nhưng triển khai tổng quát (Method Generic)
    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    // Lấy ra thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    // Lấy ra username
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Kiểm tra token đó hết hạn chưa
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Kiểm tra tính hợp lệ của token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

