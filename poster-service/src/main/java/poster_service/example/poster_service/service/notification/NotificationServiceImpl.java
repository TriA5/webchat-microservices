package poster_service.example.poster_service.service.notification;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poster_service.example.poster_service.dto.NotificationDTO;
import poster_service.example.poster_service.entity.Notification;
import poster_service.example.poster_service.entity.Notification.NotificationType;
import poster_service.example.poster_service.repository.NotificationRepository;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void createAndSendNotification(
            UUID recipientId,
            UUID actorId,
            NotificationType type,
            UUID referenceId,
            String message) {
        
        try {
            // Không gửi thông báo cho chính mình
            if (recipientId.equals(actorId)) {
                return;
            }

            // Kiểm tra thông báo đã tồn tại chưa (tránh spam)
            boolean exists = notificationRepository.existsByRecipientAndActorAndTypeAndReference(
                recipientId, actorId, type, referenceId
            );

            if (exists) {
                return; // Không tạo thông báo trùng lặp
            }

            // Tạo thông báo mới
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setActorId(actorId);
            notification.setNotificationType(type);
            notification.setReferenceId(referenceId);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            notificationRepository.save(notification);

            // Gửi thông báo realtime qua WebSocket
            NotificationDTO notificationDTO = convertToDTO(notification);
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/notifications",
                notificationDTO
            );

        } catch (Exception e) {
            e.printStackTrace();
            // Log lỗi nhưng không throw exception để không ảnh hưởng đến business logic chính
        }
    }

    @Override
    public ResponseEntity<List<NotificationDTO>> getNotifications(UUID userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationRepository.findByRecipientId(userId);
            List<NotificationDTO> dtos = notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(UUID userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationRepository.findUnreadByRecipientId(userId);
            List<NotificationDTO> dtos = notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<?> countUnreadNotifications(UUID userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            long count = notificationRepository.countUnreadByRecipientId(userId);

            return ResponseEntity.ok(java.util.Map.of(
                "userId", userId,
                "unreadCount", count,
                "message", "✅ Lấy số thông báo chưa đọc thành công"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> markAsRead(UUID notificationId, UUID userId) {
        try {
            if (notificationId == null) {
                return ResponseEntity.badRequest().body("❌ Notification ID không được để trống");
            }
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

            // Kiểm tra quyền (chỉ người nhận mới được đánh dấu đã đọc)
            if (!notification.getRecipientId().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền thao tác thông báo này");
            }

            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            notificationRepository.save(notification);

            return ResponseEntity.ok(convertToDTO(notification));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> markAllAsRead(UUID userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            notificationRepository.markAllAsReadByRecipientId(userId);

            return ResponseEntity.ok("✅ Đã đánh dấu tất cả thông báo là đã đọc");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> deleteNotification(UUID notificationId, UUID userId) {
        try {
            if (notificationId == null) {
                return ResponseEntity.badRequest().body("❌ Notification ID không được để trống");
            }
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

            // Kiểm tra quyền (chỉ người nhận mới được xóa)
            if (!notification.getRecipientId().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa thông báo này");
            }

            notificationRepository.delete(notification);

            return ResponseEntity.ok("✅ Xóa thông báo thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Helper method
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .idNotification(notification.getIdNotification())
                .recipientId(notification.getRecipientId())
                .actorId(notification.getActorId())
                .notificationType(notification.getNotificationType())
                .referenceId(notification.getReferenceId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
