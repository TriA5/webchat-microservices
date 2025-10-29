package friendship_service.example.friendship_service.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import friendship_service.example.friendship_service.client.UserDTO;
import friendship_service.example.friendship_service.entity.Friendship;
import friendship_service.example.friendship_service.service.friendship.FriendshipService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/friendships")
public class FriendShipController {
    
    @Autowired
    private FriendshipService friendshipService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from FriendShip Service!";
    }

    // 🔎 Tìm bạn qua số điện thoại
    @GetMapping("/search")
    public ResponseEntity<?> searchFriendByPhone(@RequestParam("phone") String phone) {
        try {
            UserDTO dto = friendshipService.searchByPhone(phone);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Gửi lời mời kết bạn
    @PostMapping("/send")
    public ResponseEntity<?> sendRequest(@RequestParam UUID requesterId, @RequestParam UUID addresseeId) {
        try {
            Friendship friendship = friendshipService.sendFriendRequest(requesterId, addresseeId);
            return ResponseEntity.ok(friendship);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy danh sách bạn bè (UserDTO)
    @GetMapping("/{userId}/friends")
    public ResponseEntity<?> getFriends(@PathVariable UUID userId) {
        try {
            List<UserDTO> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Chấp nhận / Từ chối lời mời
    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable UUID id, @RequestParam String action) {
        try {
            Friendship friendship = friendshipService.respondToRequest(id, action);
            return ResponseEntity.ok(friendship);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy tất cả Friendship liên quan đến user (cả người gửi và người nhận)
    @GetMapping("/{userId}/friendships")
    public ResponseEntity<?> getAllFriendships(@PathVariable UUID userId) {
        try {
            List<Friendship> friendships = friendshipService.getFriendships(userId);
            return ResponseEntity.ok(friendships);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Lấy lời mời kết bạn đang chờ (PENDING)
    @GetMapping("/{userId}/pending")
    public ResponseEntity<?> getPendingRequests(@PathVariable UUID userId) {
        try {
            List<Friendship> requests = friendshipService.getPendingRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Đếm số bạn bè
    @GetMapping("/{userId}/count")
    public ResponseEntity<?> countFriends(@PathVariable UUID userId) {
        try {
            Long count = friendshipService.countFriends(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Hủy kết bạn
    @DeleteMapping("/unfriend")
    public ResponseEntity<?> unfriend(@RequestParam UUID userId, @RequestParam UUID friendId) {
        try {
            friendshipService.unfriend(userId, friendId);
            return ResponseEntity.ok(Map.of("message", "Đã hủy kết bạn thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
