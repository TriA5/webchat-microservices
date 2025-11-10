package poster_service.example.poster_service.service.share;

import org.springframework.http.ResponseEntity;
import java.util.UUID;

public interface LikeSharePosterService {
    
    // Like bài share
    ResponseEntity<?> likeShare(UUID shareId, UUID userId);
    
    // Unlike bài share
    ResponseEntity<?> unlikeShare(UUID shareId, UUID userId);
    
    // Kiểm tra user đã like share chưa
    ResponseEntity<?> isLikedByUser(UUID shareId, UUID userId);
    
    // Đếm số like của share
    ResponseEntity<?> countLikes(UUID shareId);
    
    // Lấy danh sách user đã like share
    ResponseEntity<?> getUsersLikedShare(UUID shareId);
}
