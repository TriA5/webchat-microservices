package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity đại diện cho like trên comment của bài share poster
 */
@Data
@Entity
@Table(name = "like_comment_share_poster")
public class LikeCommentSharePoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_like_comment_share", updatable = false, nullable = false)
    private UUID idLikeCommentShare;

    // Người like comment
    @Column(name = "id_user", nullable = false)
    private UUID user;

    // Comment được like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comment_share", nullable = false)
    private CommentSharePoster commentSharePoster;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
