package friendship_service.example.friendship_service.service.friendship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import friendship_service.example.friendship_service.client.UserClient;
import friendship_service.example.friendship_service.client.UserDTO;
import friendship_service.example.friendship_service.client.ChatClient;
import friendship_service.example.friendship_service.entity.Friendship;
import friendship_service.example.friendship_service.repository.FriendshipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public UserDTO searchByPhone(String phone) {
        try {
            return userClient.searchByPhone(phone);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Friendship sendFriendRequest(UUID requesterId, UUID addresseeId) {
        // Verify cả 2 user tồn tại bằng cách gọi user-service
        UserDTO requester = userClient.getUserById(requesterId);
        UserDTO addressee = userClient.getUserById(addresseeId);
        
        if (requester == null) {
            throw new RuntimeException("Người gửi không tồn tại");
        }
        if (addressee == null) {
            throw new RuntimeException("Người nhận không tồn tại");
        }

        // Kiểm tra đã tồn tại quan hệ chưa
        Optional<Friendship> existing = friendshipRepository.findByTwoUsers(requesterId, addresseeId);
        
        if (existing.isPresent() && !"REJECTED".equals(existing.get().getStatus())) {
            throw new RuntimeException("Đã tồn tại lời mời hoặc đã là bạn bè");
        }

        Friendship friendship = new Friendship();
        friendship.setRequesterId(requesterId);
        friendship.setAddresseeId(addresseeId);
        friendship.setStatus("PENDING");
        friendship.setCreatedAt(LocalDateTime.now());
        friendship.setUpdatedAt(LocalDateTime.now());

        Friendship saved = friendshipRepository.save(friendship);

        // Gửi notification qua WebSocket cho người nhận (nếu online)
        try {
            UserDTO req = userClient.getUserById(requesterId);
            messagingTemplate.convertAndSendToUser(
                saved.getAddresseeId().toString(), 
                "/queue/friend-requests", 
                "NEW_FRIEND_REQUEST from " + (req != null ? req.getUsername() : requesterId.toString())
            );
        } catch (Exception e) {
            // ignore
        }
        
        return saved;
    }

    @Override
    public Friendship respondToRequest(UUID friendshipId, String action) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));

        if (!"PENDING".equals(friendship.getStatus())) {
            throw new RuntimeException("Lời mời này đã được xử lý");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            friendship.setStatus("ACCEPTED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            friendship.setStatus("REJECTED");
        } else {
            throw new IllegalArgumentException("Action không hợp lệ. Chỉ chấp nhận ACCEPT hoặc REJECT");
        }

        friendship.setUpdatedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(friendship);

        // Gửi notification
        try {
            messagingTemplate.convertAndSendToUser(
                ("" + ("ACCEPT".equalsIgnoreCase(action) ? friendship.getRequesterId() : friendship.getAddresseeId())),
                "/queue/friend-requests",
                "FRIEND_REQUEST_" + saved.getStatus()
            );
        } catch (Exception e) {
            // ignore
        }

        // Nếu ACCEPTED, gọi chat-service để tạo conversation
        if ("ACCEPTED".equals(saved.getStatus())) {
            try {
                java.util.Map<String, String> body = new java.util.HashMap<>();
                body.put("user1", friendship.getRequesterId().toString());
                body.put("user2", friendship.getAddresseeId().toString());
                chatClient.ensureConversation(body);
            } catch (Exception e) {
                // ignore failures
            }
        }

        return saved;
    }

    @Override
    public List<UserDTO> getFriends(UUID userId) {
        // Lấy tất cả friendships với status ACCEPTED
        List<Friendship> friendships = friendshipRepository.findFriendsByUserId(userId);
        
        List<UserDTO> friends = new ArrayList<>();
        
        for (Friendship f : friendships) {
            // Xác định ID của bạn bè (không phải chính mình)
            UUID friendId = f.getRequesterId().equals(userId) 
                ? f.getAddresseeId() 
                : f.getRequesterId();
            
            // Gọi user-service để lấy thông tin user
            UserDTO friend = userClient.getUserById(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        
        return friends;
    }

    @Override
    public List<Friendship> getFriendships(UUID userId) {
        return friendshipRepository.findByUserId(userId);
    }

    @Override
    public List<Friendship> getPendingRequests(UUID userId) {
        // Lấy các lời mời đến (userId là addressee và status = PENDING)
        return friendshipRepository.findByAddresseeIdAndStatus(userId, "PENDING");
    }

    @Override
    public void unfriend(UUID userId, UUID friendId) {
        // Tìm friendship giữa 2 người
        Optional<Friendship> friendship = friendshipRepository.findByTwoUsers(userId, friendId);
        
        if (friendship.isEmpty()) {
            throw new RuntimeException("Không tìm thấy mối quan hệ bạn bè");
        }
        
        if (!"ACCEPTED".equals(friendship.get().getStatus())) {
            throw new RuntimeException("Hai người không phải bạn bè");
        }

        // Xóa friendship
        friendshipRepository.delete(friendship.get());

        // TODO: Gửi notification qua message queue
    }

    @Override
    public Long countFriends(UUID userId) {
        return friendshipRepository.countFriendsByUserId(userId);
    }
}

