package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_notification", updatable = false, nullable = false)
    private UUID idNotification;

    // User nhận thông báo (chủ poster hoặc chủ comment)
    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    // User thực hiện hành động (like, comment, reply)
    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    // Loại thông báo
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // ID của đối tượng liên quan (poster, comment, like)
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    // Nội dung thông báo
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    // Trạng thái đã đọc
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum NotificationType {
        LIKE_POSTER,        // Ai đó like poster của bạn
        COMMENT_POSTER,     // Ai đó comment vào poster của bạn
        REPLY_COMMENT,      // Ai đó reply comment của bạn
        LIKE_COMMENT        // Ai đó like comment của bạn (nếu có)
    }
}
