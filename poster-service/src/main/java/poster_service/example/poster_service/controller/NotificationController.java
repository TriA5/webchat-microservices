package poster_service.example.poster_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import poster_service.example.poster_service.dto.NotificationDTO;
import poster_service.example.poster_service.service.notification.NotificationService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Lấy tất cả thông báo của user
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam UUID userId) {
        return notificationService.getNotifications(userId);
    }

    // Lấy thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@RequestParam UUID userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    // Đếm số thông báo chưa đọc
    @GetMapping("/unread/count")
    public ResponseEntity<?> countUnreadNotifications(@RequestParam UUID userId) {
        return notificationService.countUnreadNotifications(userId);
    }

    // Đánh dấu một thông báo là đã đọc
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable UUID notificationId,
            @RequestParam UUID userId) {
        return notificationService.markAsRead(notificationId, userId);
    }

    // Đánh dấu tất cả thông báo là đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam UUID userId) {
        return notificationService.markAllAsRead(userId);
    }

    // Xóa thông báo
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable UUID notificationId,
            @RequestParam UUID userId) {
        return notificationService.deleteNotification(notificationId, userId);
    }
}
