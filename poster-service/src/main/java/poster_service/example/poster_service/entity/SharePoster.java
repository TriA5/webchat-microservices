package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity đại diện cho bài share poster
 * Một user có thể share poster của người khác kèm nội dung riêng
 */
@Data
@Entity
@Table(name = "share_poster")
public class SharePoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_share", updatable = false, nullable = false)
    private UUID idShare;

    // Người share poster
    @Column(name = "id_user", nullable = false)
    private UUID user;

    // Poster gốc được share
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poster", nullable = false)
    private Poster poster;

    // Nội dung kèm theo khi share (có thể để trống)
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Trạng thái riêng tư của bài share
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_privacy_status", nullable = false)
    private PrivacyStatusPoster privacyStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ với Like
    @OneToMany(mappedBy = "sharePoster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeSharePoster> likes = new ArrayList<>();

    // Quan hệ với Comment
    @OneToMany(mappedBy = "sharePoster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentSharePoster> comments = new ArrayList<>();
}
