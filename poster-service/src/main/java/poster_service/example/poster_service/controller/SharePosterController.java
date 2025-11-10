package poster_service.example.poster_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import poster_service.example.poster_service.service.share.SharePosterService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/shares")
// @CrossOrigin(origins = "*")
public class SharePosterController {

    @Autowired
    private SharePosterService sharePosterService;

    /**
     * Tạo bài share poster
     * POST /api/shares
     * Body: {
     *   "posterId": "uuid",
     *   "userId": "uuid",
     *   "content": "text",
     *   "privacyStatusName": "PUBLIC|FRIENDS|PRIVATE"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createShare(@RequestBody JsonNode shareData) {
        try {
            log.info("📤 POST /api/shares - Creating share");

            if (!shareData.has("posterId") || !shareData.has("userId")) {
                return ResponseEntity.badRequest().body("❌ posterId và userId là bắt buộc!");
            }

            UUID posterId = UUID.fromString(shareData.get("posterId").asText());
            UUID userId = UUID.fromString(shareData.get("userId").asText());
            String content = shareData.has("content") ? shareData.get("content").asText() : "";
            String privacyStatusName = shareData.has("privacyStatusName") ? 
                    shareData.get("privacyStatusName").asText() : "PUBLIC";

            return sharePosterService.createShare(posterId, userId, content, privacyStatusName);
        } catch (Exception e) {
            log.error("❌ Error creating share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Cập nhật bài share
     * PUT /api/shares/{shareId}
     */
    @PutMapping("/{shareId}")
    public ResponseEntity<?> updateShare(
            @PathVariable UUID shareId,
            @RequestBody JsonNode updateData) {
        try {
            log.info("✏️ PUT /api/shares/{} - Updating share", shareId);

            if (!updateData.has("userId")) {
                return ResponseEntity.badRequest().body("❌ userId là bắt buộc!");
            }

            UUID userId = UUID.fromString(updateData.get("userId").asText());
            return sharePosterService.updateShare(shareId, userId, updateData);
        } catch (Exception e) {
            log.error("❌ Error updating share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Xóa bài share
     * DELETE /api/shares/{shareId}?userId={userId}
     */
    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> deleteShare(
            @PathVariable UUID shareId,
            @RequestParam UUID userId) {
        try {
            log.info("🗑️ DELETE /api/shares/{} - Deleting share", shareId);
            return sharePosterService.deleteShare(shareId, userId);
        } catch (Exception e) {
            log.error("❌ Error deleting share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết bài share
     * GET /api/shares/{shareId}
     */
    @GetMapping("/{shareId}")
    public ResponseEntity<?> getShareById(@PathVariable UUID shareId) {
        try {
            log.info("📖 GET /api/shares/{} - Getting share details", shareId);
            return sharePosterService.getShareById(shareId);
        } catch (Exception e) {
            log.error("❌ Error getting share: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả share của 1 user
     * GET /api/shares/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllSharesByUserId(@PathVariable UUID userId) {
        try {
            log.info("📋 GET /api/shares/user/{} - Getting shares by user", userId);
            return sharePosterService.getAllSharesByUserId(userId);
        } catch (Exception e) {
            log.error("❌ Error getting shares: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả share của 1 poster gốc
     * GET /api/shares/poster/{posterId}
     */
    @GetMapping("/poster/{posterId}")
    public ResponseEntity<?> getAllSharesByPosterId(@PathVariable UUID posterId) {
        try {
            log.info("📋 GET /api/shares/poster/{} - Getting shares of poster", posterId);
            return sharePosterService.getAllSharesByPosterId(posterId);
        } catch (Exception e) {
            log.error("❌ Error getting shares: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả share visible cho viewer
     * GET /api/shares/feed/{viewerId}
     */
    @GetMapping("/feed/{viewerId}")
    public ResponseEntity<?> getAllVisibleShares(@PathVariable UUID viewerId) {
        try {
            log.info("🔍 GET /api/shares/feed/{} - Getting visible shares", viewerId);
            return sharePosterService.getAllVisibleShares(viewerId);
        } catch (Exception e) {
            log.error("❌ Error getting shares: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Đếm số share của poster
     * GET /api/shares/count/{posterId}
     */
    @GetMapping("/count/{posterId}")
    public ResponseEntity<?> countSharesByPosterId(@PathVariable UUID posterId) {
        try {
            log.info("🔢 GET /api/shares/count/{} - Counting shares", posterId);
            return sharePosterService.countSharesByPosterId(posterId);
        } catch (Exception e) {
            log.error("❌ Error counting shares: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
}
