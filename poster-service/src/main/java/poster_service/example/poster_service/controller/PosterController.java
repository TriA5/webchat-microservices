package poster_service.example.poster_service.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import poster_service.example.poster_service.service.poster.PosterService;
@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/posters")
@RequiredArgsConstructor
public class PosterController {

    @Autowired
    private PosterService posterService;
    
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;


    @GetMapping("/hello")
    public String getPosterInfo() {
        return "Hello From Poster Service";
    }
    

    //Add
    @PostMapping
    public ResponseEntity<?> createPoster(@RequestBody JsonNode posterJson) {
        try {
            // Lấy idUser từ JSON
            String userIdStr = posterJson.get("idUser").asText();
            UUID userId = UUID.fromString(userIdStr);
            
            ResponseEntity<?> response = posterService.save(posterJson, userId);
            
            // 🔥 Broadcast poster mới qua WebSocket
            if (response.getStatusCode().is2xxSuccessful()) {
                Object posterData = response.getBody();
                System.out.println("📡 Broadcasting new poster to /topic/posters");
                if (messagingTemplate != null) {
                    messagingTemplate.convertAndSend("/topic/posters", posterData);
                } else {
                    System.err.println("SimpMessagingTemplate not available — skipping broadcast");
                }
            }
            
            return response;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    //Edit
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePoster(
            @PathVariable("id") String posterIdStr,
            @RequestBody JsonNode posterJson) {
        try {
            UUID posterId = UUID.fromString(posterIdStr);
            ResponseEntity<?> response = posterService.update(posterId, posterJson, UUID.fromString(posterJson.get("idUser").asText()));
            
            // 🔥 Broadcast poster đã update qua WebSocket
            if (response.getStatusCode().is2xxSuccessful()) {
                Object posterData = response.getBody();
                System.out.println("📡 Broadcasting updated poster to /topic/posters/updated");
                if (messagingTemplate != null) {
                    messagingTemplate.convertAndSend("/topic/posters/updated", posterData);
                } else {
                    System.err.println("SimpMessagingTemplate not available — skipping broadcast");
                }
            }
            
            return response;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
    //Delete Poster
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePoster(
            @PathVariable("id") String posterIdStr,
            @RequestBody JsonNode requestBody) {
        try {
            UUID posterId = UUID.fromString(posterIdStr);
            UUID userId = UUID.fromString(requestBody.get("idUser").asText());
            ResponseEntity<?> response = posterService.delete(posterId, userId);
            
            // 🔥 Broadcast poster đã xóa qua WebSocket
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("📡 Broadcasting deleted poster ID to /topic/posters/deleted");
                if (messagingTemplate != null) {
                    messagingTemplate.convertAndSend("/topic/posters/deleted", posterIdStr);
                } else {
                    System.err.println("SimpMessagingTemplate not available — skipping broadcast");
                }
            }
            
            return response;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
    //Get Detail Poster
    @GetMapping("/{id}")
    public ResponseEntity<?> getPosterById(@PathVariable("id") String posterIdStr) {
        try {
            UUID posterId = UUID.fromString(posterIdStr);
            return posterService.getById(posterId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    //Get All Posters by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostersByUserId(@PathVariable("userId") String userIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            return posterService.getAllByUserId(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }

    //Get All Posters
    @GetMapping
    public ResponseEntity<?> getAllPosters() {
        try {
            return posterService.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
    
    //Get All Posters Visible to User (respecting privacy settings)
    @GetMapping("/feed/{viewerId}")
    public ResponseEntity<?> getVisiblePosters(@PathVariable("viewerId") String viewerIdStr) {
        try {
            UUID viewerId = UUID.fromString(viewerIdStr);
            return posterService.getAllVisibleToUser(viewerId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ UUID không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
        }
    }
}
