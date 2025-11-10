package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity đại diện cho like trên bài share poster
 */
@Data
@Entity
@Table(name = "like_share_poster")
public class LikeSharePoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_like_share", updatable = false, nullable = false)
    private UUID idLikeShare;

    // Người like
    @Column(name = "id_user", nullable = false)
    private UUID user;

    // Bài share được like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_share", nullable = false)
    private SharePoster sharePoster;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
