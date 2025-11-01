package poster_service.example.poster_service.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "poster")
public class Poster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_poster", updatable = false, nullable = false)
    private UUID idPoster;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔹 Liên kết đến user (người đăng)
    @Column(name = "id_user", nullable = false)
    private UUID user;

    // 🔹 Liên kết đến bảng trạng thái hiển thị
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_privacy_status", nullable = false)
    private PrivacyStatusPoster privacyStatus;

    // 🔹 Quan hệ 1-nhiều với Image
    @OneToMany(mappedBy = "poster", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagePoster> images;
    // 🔹 Quan hệ 1-nhiều với LikePoster
    @OneToMany(mappedBy = "poster", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LikePoster> likes;
}

