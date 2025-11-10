package poster_service.example.poster_service.service.share;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public interface CommentSharePosterService {
    
    // Tạo comment mới trên share
    ResponseEntity<?> createComment(UUID shareId, UUID userId, String content);
    
    // Trả lời comment (nested reply)
    ResponseEntity<?> replyToComment(UUID commentId, UUID userId, String content);
    
    // Cập nhật comment
    ResponseEntity<?> updateComment(UUID commentId, UUID userId, String newContent);
    
    // Xóa comment
    ResponseEntity<?> deleteComment(UUID commentId, UUID userId);
    
    // Lấy tất cả comment của share (bao gồm replies)
    ResponseEntity<?> getAllCommentsByShareId(UUID shareId);
    
    // Lấy chi tiết 1 comment
    ResponseEntity<?> getCommentById(UUID commentId);
    
    // Like comment
    ResponseEntity<?> likeComment(UUID commentId, UUID userId);
    
    // Unlike comment
    ResponseEntity<?> unlikeComment(UUID commentId, UUID userId);
}
