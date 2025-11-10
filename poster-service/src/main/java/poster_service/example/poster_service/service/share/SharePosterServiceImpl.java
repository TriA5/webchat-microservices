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

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import poster_service.example.poster_service.client.UserClient;
import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.entity.PrivacyStatusPoster;
import poster_service.example.poster_service.entity.SharePoster;
import poster_service.example.poster_service.repository.*;
import poster_service.example.poster_service.service.poster.PosterService;

@Slf4j
@Service
public class SharePosterServiceImpl implements SharePosterService {

    @Autowired
    private SharePosterRepository sharePosterRepository;

    @Autowired
    private PosterRepository posterRepository;

    @Autowired
    private PrivacyStatusPosterRepository privacyStatusRepository;

    @Autowired
    private LikeSharePosterRepository likeSharePosterRepository;

    @Autowired
    private CommentSharePosterRepository commentSharePosterRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private poster_service.example.poster_service.client.FriendshipClient friendshipClient;

    @Override
    @Transactional
    public ResponseEntity<?> createShare(UUID posterId, UUID userId, String content, String privacyStatusName) {
        try {
            log.info("📤 Creating share for poster {} by user {}", posterId, userId);

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            // Kiểm tra poster gốc tồn tại
            Poster originalPoster = posterRepository.findById(posterId)
                    .orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));

            // Kiểm tra privacy status
            PrivacyStatusPoster privacyStatus = privacyStatusRepository.findByName(privacyStatusName)
                    .orElseThrow(() -> new RuntimeException("Privacy status không tồn tại: " + privacyStatusName));

            // Tạo share poster
            SharePoster sharePoster = new SharePoster();
            sharePoster.setUser(userId);
            sharePoster.setPoster(originalPoster);
            sharePoster.setContent(content != null ? content : "");
            sharePoster.setPrivacyStatus(privacyStatus);
            sharePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            sharePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            SharePoster savedShare = sharePosterRepository.save(sharePoster);
            log.info("✅ Share created successfully with ID: {}", savedShare.getIdShare());

            return ResponseEntity.ok(convertToDTO(savedShare));
        } catch (RuntimeException e) {
            log.error("❌ Error creating share: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateShare(UUID shareId, UUID userId, JsonNode updateData) {
        try {
            log.info("✏️ Updating share {} by user {}", shareId, userId);

            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Kiểm tra quyền sở hữu
            if (!sharePoster.getUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền chỉnh sửa share này!");
            }

            // Cập nhật content
            if (updateData.has("content")) {
                sharePoster.setContent(updateData.get("content").asText());
            }

            // Cập nhật privacy status
            if (updateData.has("privacyStatusName")) {
                String privacyStatusName = updateData.get("privacyStatusName").asText();
                PrivacyStatusPoster privacyStatus = privacyStatusRepository.findByName(privacyStatusName)
                        .orElseThrow(() -> new RuntimeException("Privacy status không tồn tại: " + privacyStatusName));
                sharePoster.setPrivacyStatus(privacyStatus);
            }

            sharePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            sharePosterRepository.save(sharePoster);

            log.info("✅ Share updated successfully");
            return ResponseEntity.ok(convertToDTO(sharePoster));
        } catch (RuntimeException e) {
            log.error("❌ Error updating share: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteShare(UUID shareId, UUID userId) {
        try {
            log.info("🗑️ Deleting share {} by user {}", shareId, userId);

            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Kiểm tra quyền sở hữu
            if (!sharePoster.getUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa share này!");
            }

            // Xóa các quan hệ phụ thuộc
            commentSharePosterRepository.deleteAllByShareId(shareId);
            likeSharePosterRepository.deleteAllByShareId(shareId);

            // Xóa share
            sharePosterRepository.delete(sharePoster);

            log.info("✅ Share deleted successfully");
            return ResponseEntity.ok("✅ Xóa share thành công!");
        } catch (RuntimeException e) {
            log.error("❌ Error deleting share: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getShareById(UUID shareId) {
        try {
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));
            return ResponseEntity.ok(convertToDTO(sharePoster));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllSharesByUserId(UUID userId) {
        try {
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            List<SharePoster> shares = sharePosterRepository.findByUserOrderByCreatedAtDesc(userId);
            List<Map<String, Object>> shareDTOs = shares.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(shareDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllSharesByPosterId(UUID posterId) {
        try {
            Poster poster = posterRepository.findById(posterId)
                    .orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));

            List<SharePoster> shares = sharePosterRepository.findByPoster_IdPosterOrderByCreatedAtDesc(posterId);
            List<Map<String, Object>> shareDTOs = shares.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(shareDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllVisibleShares(UUID viewerId) {
        try {
            log.info("🔍 Getting visible shares for viewer: {}", viewerId);

            var viewerDto = userClient.getUserById(viewerId);
            if (viewerDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + viewerId);
            }

            List<SharePoster> allShares = sharePosterRepository.findAllByOrderByCreatedAtDesc();
            log.info("📊 Total shares in database: {}", allShares.size());

            List<Map<String, Object>> visibleShares = allShares.stream()
                    .filter(share -> isVisibleToUser(share, viewerId))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("✅ Returning {} visible shares", visibleShares.size());
            return ResponseEntity.ok(visibleShares);
        } catch (RuntimeException e) {
            log.error("❌ Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> countSharesByPosterId(UUID posterId) {
        try {
            Poster poster = posterRepository.findById(posterId)
                    .orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));

            Long count = sharePosterRepository.countByPoster_IdPoster(posterId);
            Map<String, Object> result = new HashMap<>();
            result.put("posterId", posterId);
            result.put("shareCount", count);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Helper methods
    private boolean isVisibleToUser(SharePoster share, UUID viewerId) {
        String privacyName = share.getPrivacyStatus() != null ? share.getPrivacyStatus().getName() : "PUBLIC";
        UUID authorId = share.getUser();

        if ("PUBLIC".equals(privacyName)) {
            return true;
        }
        if ("PRIVATE".equals(privacyName)) {
            return authorId != null && authorId.equals(viewerId);
        }
        if ("FRIENDS".equals(privacyName)) {
            if (authorId != null && authorId.equals(viewerId)) {
                return true;
            }
            return authorId != null && areFriends(viewerId, authorId);
        }
        return false;
    }

    private boolean areFriends(UUID userId1, UUID userId2) {
        try {
            var friends = friendshipClient.getFriends(userId1);
            if (friends == null) return false;
            return friends.stream().anyMatch(u -> u.getIdUser().equals(userId2));
        } catch (Exception e) {
            log.error("Error calling friendship-service: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, Object> convertToDTO(SharePoster share) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("idShare", share.getIdShare());
        dto.put("content", share.getContent());
        dto.put("createdAt", share.getCreatedAt());
        dto.put("updatedAt", share.getUpdatedAt());

        // Thông tin người share
        if (share.getUser() != null) {
            try {
                var author = userClient.getUserById(share.getUser());
                if (author != null) {
                    dto.put("idUser", author.getIdUser());
                    dto.put("userName", author.getUsername());
                    dto.put("userFirstName", author.getFirstName());
                    dto.put("userLastName", author.getLastName());
                    dto.put("userAvatar", author.getAvatar());
                } else {
                    dto.put("idUser", share.getUser());
                }
            } catch (Exception ignored) {
                dto.put("idUser", share.getUser());
            }
        }

        // Privacy status
        if (share.getPrivacyStatus() != null) {
            dto.put("privacyStatusName", share.getPrivacyStatus().getName());
        }

        // Poster gốc
        if (share.getPoster() != null) {
            dto.put("originalPoster", convertPosterToDTO(share.getPoster()));
        }

        // Thống kê
        dto.put("likeCount", likeSharePosterRepository.countBySharePoster_IdShare(share.getIdShare()));
        dto.put("commentCount", commentSharePosterRepository.countBySharePoster_IdShare(share.getIdShare()));

        return dto;
    }

    private Map<String, Object> convertPosterToDTO(Poster poster) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("idPoster", poster.getIdPoster());
        dto.put("content", poster.getContent());
        dto.put("createdAt", poster.getCreatedAt());

        // Thông tin tác giả poster gốc
        if (poster.getUser() != null) {
            try {
                var author = userClient.getUserById(poster.getUser());
                if (author != null) {
                    dto.put("userName", author.getUsername());
                    dto.put("userAvatar", author.getAvatar());
                }
            } catch (Exception ignored) {}
        }

        return dto;
    }
}
