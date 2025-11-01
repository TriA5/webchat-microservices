package poster_service.example.poster_service.service.like;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import poster_service.example.poster_service.client.UserClient;
import poster_service.example.poster_service.entity.LikePoster;
import poster_service.example.poster_service.entity.Notification.NotificationType;
import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.repository.LikePosterRepository;
import poster_service.example.poster_service.repository.PosterRepository;
import poster_service.example.poster_service.service.notification.NotificationService;

@Service
@Transactional
public class LikeServiceImpl implements LikeService {
    //
    @Autowired
    private LikePosterRepository likePosterRepository;
    @Autowired
    private PosterRepository posterRepository;
    @Autowired
    private UserClient userClient;
    @Autowired
    private NotificationService notificationService;


    //LikePoster
    @Override
    public ResponseEntity<?> likePoster(UUID posterId , UUID userId) {
        try{
        LikePoster likePoster = new LikePoster();
        //poster
        Poster poster = posterRepository.findById(posterId).orElse(null);
        if (poster == null) {
            return ResponseEntity.badRequest().body("Poster not found");
        }else{
            likePoster.setPoster(poster);
        }
        //user
        var userDto = userClient.getUserById(userId);
        if(userDto == null){
            return ResponseEntity.badRequest().body("User not found");
        }else{
            likePoster.setIdUser(userId);
        }
        likePoster.setLiked(true);
        likePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        likePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        //save like poster
        likePosterRepository.save(likePoster);
        
        // 🔔 Gửi thông báo cho chủ poster
        notificationService.createAndSendNotification(
            poster.getUser(), // recipient: chủ poster
            userId, // actor: người like
            NotificationType.LIKE_POSTER,
            posterId, // reference: poster ID
            "đã thích bài viết của bạn"
        );
        
        return ResponseEntity.ok("Poster liked successfully");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
    }
}

    //xóa likePoster
    @Override
    public ResponseEntity<?> unlikePoster(UUID posterId, UUID userId) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().body("❌ Poster ID không được để trống");
            }
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            // Kiểm tra poster có tồn tại không
            if (!posterRepository.existsById(posterId)) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy poster với ID: " + posterId);
            }

            // Tìm like poster
            var likePosterOptional = likePosterRepository.findByPosterIdAndUserId(posterId, userId);
            
            if (likePosterOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Bạn chưa like poster này");
            }

            // Xóa like
            likePosterRepository.delete(likePosterOptional.get());
            
            return ResponseEntity.ok("✅ Unlike poster thành công");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Lấy tổng số like của poster
    @Override
    public ResponseEntity<?> getTotalLikes(UUID posterId) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().body("❌ Poster ID không được để trống");
            }

            // Kiểm tra poster có tồn tại không
            if (!posterRepository.existsById(posterId)) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy poster với ID: " + posterId);
            }

            // Đếm tổng số like
            long totalLikes = likePosterRepository.countLikesByPosterId(posterId);
            
            return ResponseEntity.ok(java.util.Map.of(
                "posterId", posterId,
                "totalLikes", totalLikes,
                "message", "✅ Lấy tổng số like thành công"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }


}
