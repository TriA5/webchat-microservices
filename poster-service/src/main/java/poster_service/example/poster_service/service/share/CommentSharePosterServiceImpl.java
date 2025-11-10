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
import poster_service.example.poster_service.entity.CommentSharePoster;
import poster_service.example.poster_service.entity.LikeCommentSharePoster;
import poster_service.example.poster_service.entity.SharePoster;
import poster_service.example.poster_service.repository.CommentSharePosterRepository;
import poster_service.example.poster_service.repository.LikeCommentSharePosterRepository;
import poster_service.example.poster_service.repository.SharePosterRepository;

@Slf4j
@Service
public class CommentSharePosterServiceImpl implements CommentSharePosterService {

    @Autowired
    private CommentSharePosterRepository commentSharePosterRepository;

    @Autowired
    private SharePosterRepository sharePosterRepository;

    @Autowired
    private LikeCommentSharePosterRepository likeCommentSharePosterRepository;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional
    public ResponseEntity<?> createComment(UUID shareId, UUID userId, String content) {
        try {
            log.info("💬 User {} commenting on share {}", userId, shareId);

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            // Kiểm tra share tồn tại
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Validate content
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Nội dung comment không được để trống");
            }

            // Tạo comment
            CommentSharePoster comment = new CommentSharePoster();
            comment.setUser(userId);
            comment.setSharePoster(sharePoster);
            comment.setContent(content.trim());
            comment.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            comment.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            CommentSharePoster savedComment = commentSharePosterRepository.save(comment);
            log.info("✅ Comment created successfully");

            // TODO: Gửi notification cho chủ share

            return ResponseEntity.ok(convertToDTO(savedComment));
        } catch (RuntimeException e) {
            log.error("❌ Error creating comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> replyToComment(UUID commentId, UUID userId, String content) {
        try {
            log.info("↩️ User {} replying to comment {}", userId, commentId);

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            // Kiểm tra comment cha tồn tại
            CommentSharePoster parentComment = commentSharePosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại với ID: " + commentId));

            // Validate content
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Nội dung reply không được để trống");
            }

            // Tạo reply
            CommentSharePoster reply = new CommentSharePoster();
            reply.setUser(userId);
            reply.setSharePoster(parentComment.getSharePoster());
            reply.setParentComment(parentComment);
            reply.setContent(content.trim());
            reply.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            reply.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            CommentSharePoster savedReply = commentSharePosterRepository.save(reply);
            log.info("✅ Reply created successfully");

            // TODO: Gửi notification cho chủ comment cha

            return ResponseEntity.ok(convertToDTO(savedReply));
        } catch (RuntimeException e) {
            log.error("❌ Error creating reply: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateComment(UUID commentId, UUID userId, String newContent) {
        try {
            log.info("✏️ User {} updating comment {}", userId, commentId);

            CommentSharePoster comment = commentSharePosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại với ID: " + commentId));

            // Kiểm tra quyền sở hữu
            if (!comment.getUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền sửa comment này!");
            }

            // Validate content
            if (newContent == null || newContent.trim().isEmpty()) {
                throw new RuntimeException("Nội dung comment không được để trống");
            }

            comment.setContent(newContent.trim());
            comment.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            commentSharePosterRepository.save(comment);
            log.info("✅ Comment updated successfully");

            return ResponseEntity.ok(convertToDTO(comment));
        } catch (RuntimeException e) {
            log.error("❌ Error updating comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteComment(UUID commentId, UUID userId) {
        try {
            log.info("🗑️ User {} deleting comment {}", userId, commentId);

            CommentSharePoster comment = commentSharePosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại với ID: " + commentId));

            // Kiểm tra quyền sở hữu
            if (!comment.getUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa comment này!");
            }

            // Xóa comment (cascade sẽ xóa replies và likes)
            commentSharePosterRepository.delete(comment);
            log.info("✅ Comment deleted successfully");

            return ResponseEntity.ok("✅ Xóa comment thành công!");
        } catch (RuntimeException e) {
            log.error("❌ Error deleting comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllCommentsByShareId(UUID shareId) {
        try {
            SharePoster sharePoster = sharePosterRepository.findById(shareId)
                    .orElseThrow(() -> new RuntimeException("Share không tồn tại với ID: " + shareId));

            // Lấy root comments (không có parent)
            List<CommentSharePoster> rootComments = commentSharePosterRepository.findRootCommentsByShareId(shareId);

            // Convert sang DTO với nested replies
            List<Map<String, Object>> commentDTOs = rootComments.stream()
                    .map(this::convertToDTOWithReplies)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(commentDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getCommentById(UUID commentId) {
        try {
            CommentSharePoster comment = commentSharePosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại với ID: " + commentId));

            return ResponseEntity.ok(convertToDTOWithReplies(comment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> likeComment(UUID commentId, UUID userId) {
        try {
            log.info("👍 User {} liking comment {}", userId, commentId);

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(userId);
            if (userDto == null) {
                throw new RuntimeException("User không tồn tại với ID: " + userId);
            }

            // Kiểm tra comment tồn tại
            CommentSharePoster comment = commentSharePosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại với ID: " + commentId));

            // Kiểm tra đã like chưa
            if (likeCommentSharePosterRepository.existsByCommentSharePoster_IdCommentShareAndUser(commentId, userId)) {
                return ResponseEntity.badRequest().body("❌ Bạn đã like comment này rồi!");
            }

            // Tạo like
            LikeCommentSharePoster like = new LikeCommentSharePoster();
            like.setUser(userId);
            like.setCommentSharePoster(comment);
            like.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            likeCommentSharePosterRepository.save(like);
            log.info("✅ Like comment successful");

            // TODO: Gửi notification cho chủ comment

            Map<String, Object> result = new HashMap<>();
            result.put("message", "✅ Like comment thành công!");
            result.put("likeCount", likeCommentSharePosterRepository.countByCommentSharePoster_IdCommentShare(commentId));

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("❌ Error liking comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> unlikeComment(UUID commentId, UUID userId) {
        try {
            log.info("👎 User {} unliking comment {}", userId, commentId);

            // Tìm like
            LikeCommentSharePoster like = likeCommentSharePosterRepository.findByCommentIdAndUserId(commentId, userId)
                    .orElseThrow(() -> new RuntimeException("Bạn chưa like comment này!"));

            // Xóa like
            likeCommentSharePosterRepository.delete(like);
            log.info("✅ Unlike comment successful");

            Map<String, Object> result = new HashMap<>();
            result.put("message", "✅ Unlike comment thành công!");
            result.put("likeCount", likeCommentSharePosterRepository.countByCommentSharePoster_IdCommentShare(commentId));

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("❌ Error unliking comment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Helper methods
    private Map<String, Object> convertToDTO(CommentSharePoster comment) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("idCommentShare", comment.getIdCommentShare());
        dto.put("content", comment.getContent());
        dto.put("createdAt", comment.getCreatedAt());
        dto.put("updatedAt", comment.getUpdatedAt());

        // User info
        if (comment.getUser() != null) {
            try {
                var userDto = userClient.getUserById(comment.getUser());
                if (userDto != null) {
                    dto.put("idUser", userDto.getIdUser());
                    dto.put("userName", userDto.getUsername());
                    dto.put("userAvatar", userDto.getAvatar());
                }
            } catch (Exception ignored) {}
        }

        // Parent comment ID (nếu là reply)
        if (comment.getParentComment() != null) {
            dto.put("parentCommentId", comment.getParentComment().getIdCommentShare());
        }

        // Statistics
        dto.put("likeCount", likeCommentSharePosterRepository.countByCommentSharePoster_IdCommentShare(comment.getIdCommentShare()));
        dto.put("replyCount", commentSharePosterRepository.countByParentComment_IdCommentShare(comment.getIdCommentShare()));

        return dto;
    }

    private Map<String, Object> convertToDTOWithReplies(CommentSharePoster comment) {
        Map<String, Object> dto = convertToDTO(comment);

        // Lấy tất cả replies (recursive)
        List<CommentSharePoster> replies = commentSharePosterRepository
                .findByParentComment_IdCommentShareOrderByCreatedAtAsc(comment.getIdCommentShare());

        if (!replies.isEmpty()) {
            dto.put("replies", replies.stream()
                    .map(this::convertToDTOWithReplies) // Recursive call
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
