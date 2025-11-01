package poster_service.example.poster_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poster_service.example.poster_service.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    // Lấy tất cả thông báo của user (sắp xếp theo mới nhất)
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientId(@Param("userId") UUID userId);
    
    // Lấy thông báo chưa đọc của user
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("userId") UUID userId);
    
    // Đếm số thông báo chưa đọc
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :userId AND n.isRead = false")
    long countUnreadByRecipientId(@Param("userId") UUID userId);
    
    // Đánh dấu tất cả thông báo là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientId = :userId AND n.isRead = false")
    void markAllAsReadByRecipientId(@Param("userId") UUID userId);
    
    // Kiểm tra thông báo đã tồn tại (tránh spam)
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n " +
           "WHERE n.recipientId = :recipientId AND n.actorId = :actorId " +
           "AND n.notificationType = :type AND n.referenceId = :referenceId")
    boolean existsByRecipientAndActorAndTypeAndReference(
        @Param("recipientId") UUID recipientId,
        @Param("actorId") UUID actorId,
        @Param("type") Notification.NotificationType type,
        @Param("referenceId") UUID referenceId
    );
}
