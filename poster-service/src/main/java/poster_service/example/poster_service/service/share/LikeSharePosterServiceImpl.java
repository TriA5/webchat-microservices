package poster_service.example.poster_service.service.share;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import poster_service.example.poster_service.client.UserClient;
import poster_service.example.poster_service.entity.LikeSharePoster;
import poster_service.example.poster_service.entity.SharePoster;
import poster_service.example.poster_service.repository.LikeSharePosterRepository;
import poster_service.example.poster_service.repository.SharePosterRepository;

@Slf4j
@Service
public class LikeSharePosterServiceImpl implements LikeSharePosterService {

    @Autowired
    private LikeSharePosterRepository likeSharePosterRepository;

    @Autowired
    private SharePosterRepository sharePosterRepository;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional
    public ResponseEntity<?> likeShare(UUID shareId, UUID userId) {
        try {
            log.info("👍 User {} liking share {}", userId, shareId);

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            // Kiểm tra share tồn tại
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Kiểm tra đã like chưa
            if (likeSharePosterRepository.existsBySharePoster_IdShareAndUser(shareId, userId)) {
                log.warn("⚠️ User {} already liked share {}", userId, shareId);
                return ResponseEntity.badRequest().body("❌ Bạn đã like share này rồi!");
            }

            // Tạo like
            LikeSharePoster like = new LikeSharePoster();
            like.setUser(userId);
            like.setSharePoster(sharePoster);
            like.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            likeSharePosterRepository.save(like);
            log.info("✅ Like created successfully");

            // TODO: Gửi notification cho chủ share (nếu không phải tự like)

            Map<String, Object> result = new HashMap<>();
            result.put("message", "✅ Like share thành công!");
            result.put("likeCount", likeSharePosterRepository.countBySharePoster_IdShare(shareId));

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("❌ Error liking share: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> unlikeShare(UUID shareId, UUID userId) {
        try {
            log.info("👎 User {} unliking share {}", userId, shareId);

            // Kiểm tra share tồn tại
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Tìm like
            LikeSharePoster like = likeSharePosterRepository.findByShareIdAndUserId(shareId, userId)
                    .orElseThrow(() -> new RuntimeException("Bạn chưa like share này!"));

            // Xóa like
            likeSharePosterRepository.delete(like);
            log.info("✅ Unlike successful");

            Map<String, Object> result = new HashMap<>();
            result.put("message", "✅ Unlike share thành công!");
            result.put("likeCount", likeSharePosterRepository.countBySharePoster_IdShare(shareId));

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("❌ Error unliking share: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> isLikedByUser(UUID shareId, UUID userId) {
        try {
            boolean isLiked = likeSharePosterRepository.existsBySharePoster_IdShareAndUser(shareId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> countLikes(UUID shareId) {
        try {
            Long count = likeSharePosterRepository.countBySharePoster_IdShare(shareId);
            Map<String, Object> result = new HashMap<>();
            result.put("likeCount", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getUsersLikedShare(UUID shareId) {
        try {
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            List<LikeSharePoster> likes = sharePoster.getLikes();
            List<Map<String, Object>> users = likes.stream()
                    .map(like -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        try {
                            var userDto = userClient.getUserById(like.getUser());
                            if (userDto != null) {
                                userInfo.put("idUser", userDto.getIdUser());
                                userInfo.put("username", userDto.getUsername());
                                userInfo.put("avatar", userDto.getAvatar());
                            }
                        } catch (Exception ignored) {}
                        return userInfo;
                    })
                    .filter(map -> !map.isEmpty())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }
}
