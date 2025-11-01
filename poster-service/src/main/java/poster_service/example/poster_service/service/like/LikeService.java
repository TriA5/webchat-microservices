package poster_service.example.poster_service.service.like;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

public interface LikeService {
    public ResponseEntity<?> likePoster(UUID posterId , UUID userId);

    public ResponseEntity<?> unlikePoster(UUID posterId , UUID userId);
    
    public ResponseEntity<?> getTotalLikes(UUID posterId);
}
