package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "like_posters")
public class LikePoster {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_like_poster", updatable = false, nullable = false)
    private UUID idLikePoster;

    @Column(name = "is_liked", nullable = false)
    private boolean isLiked;

    @Column(name = "id_user", nullable = false)
    private UUID idUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔹 Mỗi like thuộc 1 poster
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poster", nullable = false)
    private Poster poster;
}
