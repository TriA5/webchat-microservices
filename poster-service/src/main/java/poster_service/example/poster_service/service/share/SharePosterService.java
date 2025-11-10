package poster_service.example.poster_service.service.share;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public interface SharePosterService {
    
    // Tạo bài share poster
    ResponseEntity<?> createShare(UUID posterId, UUID userId, String content, String privacyStatusName);
    
    // Cập nhật nội dung share
    ResponseEntity<?> updateShare(UUID shareId, UUID userId, JsonNode updateData);
    
    // Xóa bài share
    ResponseEntity<?> deleteShare(UUID shareId, UUID userId);
    
    // Lấy chi tiết 1 bài share
    ResponseEntity<?> getShareById(UUID shareId);
    
    // Lấy tất cả share của 1 user
    ResponseEntity<?> getAllSharesByUserId(UUID userId);
    
    // Lấy tất cả share của 1 poster gốc
    ResponseEntity<?> getAllSharesByPosterId(UUID posterId);
    
    // Lấy tất cả share poster visible cho viewer
    ResponseEntity<?> getAllVisibleShares(UUID viewerId);
    
    // Đếm số lần poster được share
    ResponseEntity<?> countSharesByPosterId(UUID posterId);
}
