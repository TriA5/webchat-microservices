package poster_service.example.poster_service.service.comment;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import poster_service.example.poster_service.dto.CommentDTO;
import poster_service.example.poster_service.dto.CreateCommentRequest;

public interface CommentService {
    
    // Tạo comment mới cho poster
    ResponseEntity<?> createComment(UUID posterId, CreateCommentRequest request);
    
    // Tạo reply cho một comment
    ResponseEntity<?> replyToComment(UUID posterId, UUID parentCommentId, CreateCommentRequest request);
    
    // Lấy tất cả comment của một poster (bao gồm replies)
    ResponseEntity<List<CommentDTO>> getCommentsByPosterId(UUID posterId);
    
    // Xóa comment
    ResponseEntity<?> deleteComment(UUID commentId, UUID userId);
    
    // Cập nhật comment
    ResponseEntity<?> updateComment(UUID commentId, UUID userId, String newContent);
    
    // Đếm tổng số comment của poster
    ResponseEntity<?> getTotalComments(UUID posterId);
}
