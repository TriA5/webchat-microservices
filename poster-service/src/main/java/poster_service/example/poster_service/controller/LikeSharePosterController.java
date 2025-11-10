package poster_service.example.poster_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import poster_service.example.poster_service.service.share.LikeSharePosterService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/shares")
// @CrossOrigin(origins = "*")
public class LikeSharePosterController {

    @Autowired
    private LikeSharePosterService likeSharePosterService;

    /**
     * Like bài share
     * POST /api/shares/{shareId}/like
     * Body: { "userId": "uuid" }
     */
    @PostMapping("/{shareId}/like")
    public ResponseEntity<?> likeShare(
            @PathVariable UUID shareId,
            @RequestBody JsonNode data) {
        try {
            log.info("👍 POST /api/shares/{}/like", shareId);

            if (!data.has("userId")) {
                return ResponseEntity.badRequest().body("❌ userId là bắt buộc!");
            }

            UUID userId = UUID.fromString(data.get("userId").asText());
            return likeSharePosterService.likeShare(shareId, userId);
        } catch (Exception e) {
            log.error("❌ Error liking share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Unlike bài share
     * DELETE /api/shares/{shareId}/like?userId={userId}
     */
    @DeleteMapping("/{shareId}/like")
    public ResponseEntity<?> unlikeShare(
            @PathVariable UUID shareId,
            @RequestParam UUID userId) {
        try {
            log.info("👎 DELETE /api/shares/{}/like", shareId);
            return likeSharePosterService.unlikeShare(shareId, userId);
        } catch (Exception e) {
            log.error("❌ Error unliking share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra user đã like share chưa
     * GET /api/shares/{shareId}/like/check?userId={userId}
     */
    @GetMapping("/{shareId}/like/check")
    public ResponseEntity<?> isLikedByUser(
            @PathVariable UUID shareId,
            @RequestParam UUID userId) {
        try {
            log.info("🔍 GET /api/shares/{}/like/check", shareId);
            return likeSharePosterService.isLikedByUser(shareId, userId);
        } catch (Exception e) {
            log.error("❌ Error checking like: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Đếm số like của share
     * GET /api/shares/{shareId}/like/count
     */
    @GetMapping("/{shareId}/like/count")
    public ResponseEntity<?> countLikes(@PathVariable UUID shareId) {
        try {
            log.info("🔢 GET /api/shares/{}/like/count", shareId);
            return likeSharePosterService.countLikes(shareId);
        } catch (Exception e) {
            log.error("❌ Error counting likes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách user đã like share
     * GET /api/shares/{shareId}/likes
     */
    @GetMapping("/{shareId}/likes")
    public ResponseEntity<?> getUsersLikedShare(@PathVariable UUID shareId) {
        try {
            log.info("📋 GET /api/shares/{}/likes", shareId);
            return likeSharePosterService.getUsersLikedShare(shareId);
        } catch (Exception e) {
            log.error("❌ Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
}
