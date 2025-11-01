package poster_service.example.poster_service.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import poster_service.example.poster_service.service.like.LikeService;

@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/like-posters")
@RequiredArgsConstructor
public class LikePosterController {
    @Autowired
    private LikeService likeService;

    // Like a poster
    @PostMapping("/{posterId}")
    public ResponseEntity<?> likePoster(@PathVariable UUID posterId, @RequestParam UUID userId) {
        return likeService.likePoster(posterId, userId);
    }

    // Unlike a poster
    @DeleteMapping("/{posterId}")
    public ResponseEntity<?> unlikePoster(@PathVariable UUID posterId, @RequestParam UUID userId) {
        return likeService.unlikePoster(posterId, userId);
    }

    // Get total likes of a poster
    @GetMapping("/{posterId}/total")
    public ResponseEntity<?> getTotalLikes(@PathVariable UUID posterId) {
        return likeService.getTotalLikes(posterId);
    }
}
