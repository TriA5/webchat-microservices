package poster_service.example.poster_service.service.comment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poster_service.example.poster_service.client.UserClient;
import poster_service.example.poster_service.dto.CommentDTO;
import poster_service.example.poster_service.dto.CreateCommentRequest;
import poster_service.example.poster_service.entity.CommentPoster;
import poster_service.example.poster_service.entity.Notification.NotificationType;
import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.repository.CommentPosterRepository;
import poster_service.example.poster_service.repository.PosterRepository;
import poster_service.example.poster_service.service.notification.NotificationService;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentPosterRepository commentPosterRepository;

    @Autowired
    private PosterRepository posterRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ResponseEntity<?> createComment(UUID posterId, CreateCommentRequest request) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().body("❌ Poster ID không được để trống");
            }
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Nội dung comment không được để trống");
            }

            // Kiểm tra poster tồn tại
            Poster poster = posterRepository.findById(posterId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy poster với ID: " + posterId));

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(request.getUserId());
            if (userDto == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy user với ID: " + request.getUserId());
            }

            // Tạo comment mới
            CommentPoster comment = new CommentPoster();
            comment.setContent(request.getContent().trim());
            comment.setIdUser(request.getUserId());
            comment.setPoster(poster);
            comment.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            comment.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            commentPosterRepository.save(comment);

            // 🔔 Gửi thông báo cho chủ poster
            notificationService.createAndSendNotification(
                poster.getUser(), // recipient: chủ poster
                request.getUserId(), // actor: người comment
                NotificationType.COMMENT_POSTER,
                comment.getIdComment(), // reference: comment ID
                "đã bình luận vào bài viết của bạn"
            );

            CommentDTO responseDTO = convertToDTO(comment);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> replyToComment(UUID posterId, UUID parentCommentId, CreateCommentRequest request) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().body("❌ Poster ID không được để trống");
            }
            if (parentCommentId == null) {
                return ResponseEntity.badRequest().body("❌ Parent Comment ID không được để trống");
            }
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Nội dung reply không được để trống");
            }

            // Kiểm tra poster tồn tại
            Poster poster = posterRepository.findById(posterId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy poster với ID: " + posterId));

            // Kiểm tra parent comment tồn tại
            CommentPoster parentComment = commentPosterRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy comment với ID: " + parentCommentId));

            // Kiểm tra parent comment có thuộc poster này không
            if (!parentComment.getPoster().getIdPoster().equals(posterId)) {
                return ResponseEntity.badRequest().body("❌ Comment không thuộc poster này");
            }

            // Kiểm tra user tồn tại
            var userDto = userClient.getUserById(request.getUserId());
            if (userDto == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy user với ID: " + request.getUserId());
            }

            // Tạo reply
            CommentPoster reply = new CommentPoster();
            reply.setContent(request.getContent().trim());
            reply.setIdUser(request.getUserId());
            reply.setPoster(poster);
            reply.setParentComment(parentComment);
            reply.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            reply.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            commentPosterRepository.save(reply);

            // 🔔 Gửi thông báo cho chủ comment gốc
            notificationService.createAndSendNotification(
                parentComment.getIdUser(), // recipient: chủ comment gốc
                request.getUserId(), // actor: người reply
                NotificationType.REPLY_COMMENT,
                reply.getIdComment(), // reference: reply ID
                "đã trả lời bình luận của bạn"
            );

            CommentDTO responseDTO = convertToDTO(reply);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<CommentDTO>> getCommentsByPosterId(UUID posterId) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().build();
            }

            // Kiểm tra poster tồn tại
            if (!posterRepository.existsById(posterId)) {
                return ResponseEntity.badRequest().build();
            }

            // Lấy tất cả root comments
            List<CommentPoster> rootComments = commentPosterRepository.findRootCommentsByPosterId(posterId);

            // Convert sang DTO và load replies
            List<CommentDTO> commentDTOs = rootComments.stream()
                    .map(this::convertToDTOWithReplies)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(commentDTOs);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<?> deleteComment(UUID commentId, UUID userId) {
        try {
            // Validate input
            if (commentId == null) {
                return ResponseEntity.badRequest().body("❌ Comment ID không được để trống");
            }
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }

            // Tìm comment
            CommentPoster comment = commentPosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy comment với ID: " + commentId));

            // Kiểm tra quyền xóa (chỉ người tạo mới được xóa)
            if (!comment.getIdUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa comment này");
            }

            // Xóa comment (cascade sẽ xóa cả replies)
            commentPosterRepository.delete(comment);

            return ResponseEntity.ok("✅ Xóa comment thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateComment(UUID commentId, UUID userId, String newContent) {
        try {
            // Validate input
            if (commentId == null) {
                return ResponseEntity.badRequest().body("❌ Comment ID không được để trống");
            }
            if (userId == null) {
                return ResponseEntity.badRequest().body("❌ User ID không được để trống");
            }
            if (newContent == null || newContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Nội dung mới không được để trống");
            }

            // Tìm comment
            CommentPoster comment = commentPosterRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy comment với ID: " + commentId));

            // Kiểm tra quyền sửa (chỉ người tạo mới được sửa)
            if (!comment.getIdUser().equals(userId)) {
                return ResponseEntity.status(403).body("❌ Bạn không có quyền sửa comment này");
            }

            // Cập nhật content
            comment.setContent(newContent.trim());
            comment.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            commentPosterRepository.save(comment);

            CommentDTO responseDTO = convertToDTO(comment);
            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getTotalComments(UUID posterId) {
        try {
            // Validate input
            if (posterId == null) {
                return ResponseEntity.badRequest().body("❌ Poster ID không được để trống");
            }

            // Kiểm tra poster tồn tại
            if (!posterRepository.existsById(posterId)) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy poster với ID: " + posterId);
            }

            // Đếm số comment gốc và tổng số comment (bao gồm replies)
            long rootComments = commentPosterRepository.countRootCommentsByPosterId(posterId);
            long totalComments = commentPosterRepository.countTotalCommentsByPosterId(posterId);

            return ResponseEntity.ok(java.util.Map.of(
                    "posterId", posterId,
                    "rootComments", rootComments,
                    "totalComments", totalComments,
                    "replies", totalComments - rootComments,
                    "message", "✅ Lấy thống kê comment thành công"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    // Helper method: Convert entity sang DTO (không load replies)
    private CommentDTO convertToDTO(CommentPoster comment) {
        return CommentDTO.builder()
                .idComment(comment.getIdComment())
                .content(comment.getContent())
                .idUser(comment.getIdUser())
                .idPoster(comment.getPoster().getIdPoster())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getIdComment() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(new ArrayList<>())
                .replyCount(0)
                .build();
    }

    // Helper method: Convert entity sang DTO (có load replies)
    private CommentDTO convertToDTOWithReplies(CommentPoster comment) {
        // Load replies từ database
        List<CommentPoster> replyEntities = commentPosterRepository
                .findRepliesByParentCommentId(comment.getIdComment());

        // Convert replies sang DTO
        List<CommentDTO> replyDTOs = replyEntities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return CommentDTO.builder()
                .idComment(comment.getIdComment())
                .content(comment.getContent())
                .idUser(comment.getIdUser())
                .idPoster(comment.getPoster().getIdPoster())
                .parentCommentId(null) // root comment không có parent
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replyDTOs)
                .replyCount(replyDTOs.size())
                .build();
    }
}
