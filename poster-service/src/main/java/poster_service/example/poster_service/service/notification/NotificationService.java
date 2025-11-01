package poster_service.example.poster_service.service.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import poster_service.example.poster_service.dto.NotificationDTO;
import poster_service.example.poster_service.entity.Notification.NotificationType;

public interface NotificationService {
    
    // Tạo và gửi thông báo
    void createAndSendNotification(
        UUID recipientId, 
        UUID actorId, 
        NotificationType type, 
        UUID referenceId, 
        String message
    );
    
    // Lấy tất cả thông báo của user
    ResponseEntity<List<NotificationDTO>> getNotifications(UUID userId);
    
    // Lấy thông báo chưa đọc
    ResponseEntity<List<NotificationDTO>> getUnreadNotifications(UUID userId);
    
    // Đếm số thông báo chưa đọc
    ResponseEntity<?> countUnreadNotifications(UUID userId);
    
    // Đánh dấu một thông báo là đã đọc
    ResponseEntity<?> markAsRead(UUID notificationId, UUID userId);
    
    // Đánh dấu tất cả thông báo là đã đọc
    ResponseEntity<?> markAllAsRead(UUID userId);
    
    // Xóa thông báo
    ResponseEntity<?> deleteNotification(UUID notificationId, UUID userId);
}
