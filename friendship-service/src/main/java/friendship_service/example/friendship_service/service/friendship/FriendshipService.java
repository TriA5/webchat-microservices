package friendship_service.example.friendship_service.service.friendship;

import friendship_service.example.friendship_service.client.UserDTO;
import friendship_service.example.friendship_service.entity.Friendship;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {
    
    // Tìm user theo số điện thoại (gọi user-service)
    UserDTO searchByPhone(String phone);
    
    // Gửi lời mời kết bạn
    Friendship sendFriendRequest(UUID requesterId, UUID addresseeId);
    
    // Chấp nhận hoặc từ chối lời mời kết bạn
    Friendship respondToRequest(UUID friendshipId, String action); // ACCEPT | REJECT
    
    // Lấy danh sách bạn bè (trả về UserDTO)
    List<UserDTO> getFriends(UUID userId);
    
    // Lấy tất cả quan hệ friendship của user
    List<Friendship> getFriendships(UUID userId);
    
    // Lấy lời mời kết bạn đang chờ (PENDING)
    List<Friendship> getPendingRequests(UUID userId);
    
    // Hủy kết bạn
    void unfriend(UUID userId, UUID friendId);
    
    // Đếm số bạn bè
    Long countFriends(UUID userId);
}

