package poster_service.example.poster_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import poster_service.example.poster_service.service.share.CommentSharePosterService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/shares")
// @CrossOrigin(origins = "*")
public class CommentSharePosterController {

    @Autowired
    private CommentSharePosterService commentSharePosterService;

    /**
     * Tạo comment mới trên share
     * POST /api/shares/{shareId}/comments
     * Body: { "userId": "uuid", "content": "text" }
     */
    @PostMapping("/{shareId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable UUID shareId,
            @RequestBody JsonNode data) {
        try {
            log.info("💬 POST /api/shares/{}/comments", shareId);

            if (!data.has("userId") || !data.has("content")) {
                return ResponseEntity.badRequest().body("❌ userId và content là bắt buộc!");
            }

            UUID userId = UUID.fromString(data.get("userId").asText());
            String content = data.get("content").asText();

            return commentSharePosterService.createComment(shareId, userId, content);
        } catch (Exception e) {
            log.error("❌ Error creating comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Trả lời comment
     * POST /api/shares/comments/{commentId}/reply
     * Body: { "userId": "uuid", "content": "text" }
     */
    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<?> replyToComment(
            @PathVariable UUID commentId,
            @RequestBody JsonNode data) {
        try {
            log.info("↩️ POST /api/shares/comments/{}/reply", commentId);

            if (!data.has("userId") || !data.has("content")) {
                return ResponseEntity.badRequest().body("❌ userId và content là bắt buộc!");
            }

            UUID userId = UUID.fromString(data.get("userId").asText());
            String content = data.get("content").asText();

            return commentSharePosterService.replyToComment(commentId, userId, content);
        } catch (Exception e) {
            log.error("❌ Error replying to comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Cập nhật comment
     * PUT /api/shares/comments/{commentId}
     * Body: { "userId": "uuid", "content": "new text" }
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable UUID commentId,
            @RequestBody JsonNode data) {
        try {
            log.info("✏️ PUT /api/shares/comments/{}", commentId);

            if (!data.has("userId") || !data.has("content")) {
                return ResponseEntity.badRequest().body("❌ userId và content là bắt buộc!");
            }

            UUID userId = UUID.fromString(data.get("userId").asText());
            String content = data.get("content").asText();

            return commentSharePosterService.updateComment(commentId, userId, content);
        } catch (Exception e) {
            log.error("❌ Error updating comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Xóa comment
     * DELETE /api/shares/comments/{commentId}?userId={userId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable UUID commentId,
            @RequestParam UUID userId) {
        try {
            log.info("🗑️ DELETE /api/shares/comments/{}", commentId);
            return commentSharePosterService.deleteComment(commentId, userId);
        } catch (Exception e) {
            log.error("❌ Error deleting comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả comment của share
     * GET /api/shares/{shareId}/comments
     */
    @GetMapping("/{shareId}/comments")
    public ResponseEntity<?> getAllCommentsByShareId(@PathVariable UUID shareId) {
        try {
            log.info("📋 GET /api/shares/{}/comments", shareId);
            return commentSharePosterService.getAllCommentsByShareId(shareId);
        } catch (Exception e) {
            log.error("❌ Error getting comments: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết 1 comment
     * GET /api/shares/comments/{commentId}
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable UUID commentId) {
        try {
            log.info("📖 GET /api/shares/comments/{}", commentId);
            return commentSharePosterService.getCommentById(commentId);
        } catch (Exception e) {
            log.error("❌ Error getting comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Like comment
     * POST /api/shares/comments/{commentId}/like
     * Body: { "userId": "uuid" }
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable UUID commentId,
            @RequestBody JsonNode data) {
        try {
            log.info("👍 POST /api/shares/comments/{}/like", commentId);

            if (!data.has("userId")) {
                return ResponseEntity.badRequest().body("❌ userId là bắt buộc!");
            }

            UUID userId = UUID.fromString(data.get("userId").asText());
            return commentSharePosterService.likeComment(commentId, userId);
        } catch (Exception e) {
            log.error("❌ Error liking comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * Unlike comment
     * DELETE /api/shares/comments/{commentId}/like?userId={userId}
     */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<?> unlikeComment(
            @PathVariable UUID commentId,
            @RequestParam UUID userId) {
        try {
            log.info("👎 DELETE /api/shares/comments/{}/like", commentId);
            return commentSharePosterService.unlikeComment(commentId, userId);
        } catch (Exception e) {
            log.error("❌ Error unliking comment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
}
