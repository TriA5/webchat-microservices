package poster_service.example.poster_service.entity;


import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "privacy_status")
public class PrivacyStatusPoster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_privacy_status", updatable = false, nullable = false)
    private UUID idPrivacyStatus;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // PUBLIC, FRIENDS, PRIVATE

    @Column(name = "description", length = 255)
    private String description; // Mô tả (ví dụ: "Chỉ mình tôi", "Công khai", ...)

    @OneToMany(mappedBy = "privacyStatus", cascade = CascadeType.ALL)
    private List<Poster> posters;
}

