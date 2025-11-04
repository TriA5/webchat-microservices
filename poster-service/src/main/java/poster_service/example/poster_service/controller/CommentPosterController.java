package poster_service.example.poster_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import poster_service.example.poster_service.dto.CommentDTO;
import poster_service.example.poster_service.dto.CreateCommentRequest;
import poster_service.example.poster_service.service.comment.CommentService;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentPosterController {

    @Autowired
    private CommentService commentService;

    // Tạo comment mới cho poster
    @PostMapping("/{posterId}")
    public ResponseEntity<?> createComment(
            @PathVariable UUID posterId,
            @RequestBody CreateCommentRequest request) {
        return commentService.createComment(posterId, request);
    }

    // Tạo reply cho một comment
    @PostMapping("/{posterId}/{parentCommentId}/reply")
    public ResponseEntity<?> replyToComment(
            @PathVariable UUID posterId,
            @PathVariable UUID parentCommentId,
            @RequestBody CreateCommentRequest request) {
        return commentService.replyToComment(posterId, parentCommentId, request);
    }

    // Lấy tất cả comment của poster (bao gồm replies)
    @GetMapping("/{posterId}")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable UUID posterId) {
        return commentService.getCommentsByPosterId(posterId);
    }

    // Xóa comment
    @DeleteMapping("/{posterId}/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable UUID posterId,
            @PathVariable UUID commentId,
            @RequestParam UUID userId) {
        return commentService.deleteComment(commentId, userId);
    }

    // Cập nhật comment
    @PutMapping("/{posterId}/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable UUID posterId,
            @PathVariable UUID commentId,
            @RequestParam UUID userId,
            @RequestParam String content) {
        return commentService.updateComment(commentId, userId, content);
    }

    // Lấy thống kê comment của poster
    @GetMapping("/{posterId}/statistics")
    public ResponseEntity<?> getCommentStatistics(@PathVariable UUID posterId) {
        return commentService.getTotalComments(posterId);
    }

    // Lấy comment theo Id
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable UUID commentId) {
        return commentService.getCommentById(commentId);
    }
}
