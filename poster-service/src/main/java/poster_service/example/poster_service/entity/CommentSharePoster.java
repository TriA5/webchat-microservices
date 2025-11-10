package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity đại diện cho comment trên bài share poster
 * Hỗ trợ nested replies (comment trả lời comment)
 */
@Data
@Entity
@Table(name = "comment_share_poster")
public class CommentSharePoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_comment_share", updatable = false, nullable = false)
    private UUID idCommentShare;

    // Người comment
    @Column(name = "id_user", nullable = false)
    private UUID user;

    // Bài share được comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_share", nullable = false)
    private SharePoster sharePoster;

    // Nội dung comment
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // Comment cha (nếu là reply)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private CommentSharePoster parentComment;

    // Danh sách replies
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentSharePoster> replies = new ArrayList<>();

    // Quan hệ với Like Comment
    @OneToMany(mappedBy = "commentSharePoster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeCommentSharePoster> likes = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
