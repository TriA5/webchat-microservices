package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "video")
public class VideoPoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_video", updatable = false, nullable = false)
    private UUID idVideo;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration")
    private Integer duration; // Thời lượng video (giây)

    @Column(name = "file_size")
    private Long fileSize; // Kích thước file (bytes)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔹 Mỗi video thuộc 1 poster
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poster", nullable = false)
    private Poster poster;
}
