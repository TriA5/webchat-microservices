package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "image")
public class ImagePoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_image", updatable = false, nullable = false)
    private UUID idImage;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔹 Mỗi ảnh thuộc 1 poster
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poster", nullable = false)
    private Poster poster;
}

