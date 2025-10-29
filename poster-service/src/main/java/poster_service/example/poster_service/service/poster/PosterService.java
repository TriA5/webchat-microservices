package poster_service.example.poster_service.service.poster;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;


public interface PosterService {

    public ResponseEntity<?> save(JsonNode posterJson, UUID userId);
    
    public ResponseEntity<?> update(UUID posterId, JsonNode posterJson, UUID userId);
    
    public ResponseEntity<?> delete(UUID posterId, UUID userId);
    
    public ResponseEntity<?> getById(UUID posterId);
    
    public ResponseEntity<?> getAllByUserId(UUID userId);
    
    public ResponseEntity<?> getAll();
    
    // Get all posters visible to a specific user (based on privacy settings)
    public ResponseEntity<?> getAllVisibleToUser(UUID viewerId);

}

