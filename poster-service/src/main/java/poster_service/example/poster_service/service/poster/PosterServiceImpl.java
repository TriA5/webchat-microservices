
package poster_service.example.poster_service.service.poster;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import poster_service.example.poster_service.client.UploadClient;
import poster_service.example.poster_service.entity.ImagePoster;
import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.entity.PrivacyStatusPoster;
import poster_service.example.poster_service.repository.CommentPosterRepository;
import poster_service.example.poster_service.repository.ImagePosterRepository;
import poster_service.example.poster_service.repository.LikePosterRepository;
import poster_service.example.poster_service.repository.PosterRepository;
import poster_service.example.poster_service.repository.PrivacyStatusPosterRepository;

@Service
@Transactional
public class PosterServiceImpl implements PosterService {

private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PosterServiceImpl.class);

@Autowired
private poster_service.example.poster_service.client.UserClient userClient;

@Autowired
private PrivacyStatusPosterRepository privacyStatusRepository;

@Autowired
private PosterRepository posterRepository;

@Autowired
private ImagePosterRepository imagePosterRepository;

@Autowired
private LikePosterRepository likePosterRepository;

@Autowired
private CommentPosterRepository commentPosterRepository;

@Autowired
private poster_service.example.poster_service.client.FriendshipClient friendshipClient;

@Autowired
private UploadClient uploadClient;

private final ObjectMapper objectMapper;

public PosterServiceImpl(ObjectMapper objectMapper) {
this.objectMapper = objectMapper;
}

@Override
public ResponseEntity<?> save(JsonNode posterJson, UUID userId) {
try {
    var userDto = userClient.getUserById(userId);
    if (userDto == null) throw new RuntimeException("User không tồn tại với ID: " + userId);

    String privacyStatusName = posterJson.get("privacyStatusName").asText();
    PrivacyStatusPoster privacyStatus = privacyStatusRepository.findByName(privacyStatusName)
            .orElseThrow(() -> new RuntimeException("Privacy status không tồn tại: " + privacyStatusName));

    Poster poster = new Poster();
    poster.setContent(posterJson.get("content").asText());
    poster.setUser(userDto.getIdUser());
    poster.setPrivacyStatus(privacyStatus);
    poster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    poster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

    Poster newPoster = posterRepository.save(poster);

    if (posterJson.has("imageUrls") && posterJson.get("imageUrls").isArray()) {
        List<String> imageList = objectMapper.readValue(posterJson.get("imageUrls").traverse(), new TypeReference<List<String>>() {});
        for (int i = 0; i < imageList.size(); i++) {
            String item = imageList.get(i);
            if (item != null && item.startsWith("data:")) {
                var body = java.util.Map.of("name", "poster_" + newPoster.getIdPoster() + "_" + i, "data", item);
                String imageUrl = uploadClient.uploadBase64(body);

                ImagePoster imagePoster = new ImagePoster();
                imagePoster.setPoster(newPoster);
                imagePoster.setUrl(imageUrl);
                imagePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePosterRepository.save(imagePoster);
            } else if (item != null && !item.isBlank()) {
                ImagePoster imagePoster = new ImagePoster();
                imagePoster.setPoster(newPoster);
                imagePoster.setUrl(item);
                imagePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePosterRepository.save(imagePoster);
            }
        }
    }

    return ResponseEntity.ok("✅ Tạo poster thành công!");
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

@Override
public ResponseEntity<?> update(UUID posterId, JsonNode posterJson, UUID userId) {
try {
    Poster poster = posterRepository.findById(posterId).orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));
    if (!poster.getUser().equals(userId)) return ResponseEntity.status(403).body("❌ Bạn không có quyền chỉnh sửa poster này!");

    if (posterJson.has("content")) poster.setContent(posterJson.get("content").asText());
    if (posterJson.has("privacyStatusName")) {
        String privacyStatusName = posterJson.get("privacyStatusName").asText();
        PrivacyStatusPoster privacyStatus = privacyStatusRepository.findByName(privacyStatusName)
                .orElseThrow(() -> new RuntimeException("Privacy status không tồn tại: " + privacyStatusName));
        poster.setPrivacyStatus(privacyStatus);
    }

    poster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

    if (posterJson.has("imageUrls") && posterJson.get("imageUrls").isArray()) {
        List<String> imageList = objectMapper.readValue(posterJson.get("imageUrls").traverse(), new TypeReference<List<String>>() {});
        List<ImagePoster> oldImages = imagePosterRepository.findByPoster(poster);
        List<ImagePoster> imagesToDelete = oldImages.stream().filter(img -> !imageList.contains(img.getUrl())).collect(java.util.stream.Collectors.toList());

        for (ImagePoster imageToDelete : imagesToDelete) {
            try {
                imagePosterRepository.delete(imageToDelete);
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
            }
        }

        for (String imageUrl : imageList) {
            if (imageUrl != null && imageUrl.startsWith("data:")) {
                var body = java.util.Map.of("name", "poster_" + poster.getIdPoster() + "_" + System.currentTimeMillis(), "data", imageUrl);
                String uploadedUrl = uploadClient.uploadBase64(body);

                ImagePoster imagePoster = new ImagePoster();
                imagePoster.setPoster(poster);
                imagePoster.setUrl(uploadedUrl);
                imagePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                imagePosterRepository.save(imagePoster);
            }
        }
    }

    posterRepository.save(poster);
    return ResponseEntity.ok("✅ Cập nhật poster thành công!");
} catch (RuntimeException e) {
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

@Override
public ResponseEntity<?> delete(UUID posterId, UUID userId) {
try {
    Poster poster = posterRepository.findById(posterId).orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));
    if (!poster.getUser().equals(userId)) return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa poster này!");

    // Xóa các quan hệ phụ thuộc trước khi xóa poster để tránh vi phạm ràng buộc khóa ngoại
    commentPosterRepository.deleteAllByPosterId(posterId);
    likePosterRepository.deleteAllByPosterId(posterId);

    List<ImagePoster> images = imagePosterRepository.findByPoster(poster);
    for (ImagePoster image : images) {
        try {
            // No remote delete; just remove DB record
            uploadClient.deleteByImageUrl(image.getUrl());
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa ảnh từ DB: " + e.getMessage());
        }
    }

    posterRepository.delete(poster);
    return ResponseEntity.ok("✅ Xóa poster thành công!");
} catch (RuntimeException e) {
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

@Override
public ResponseEntity<?> getById(UUID posterId) {
try {
    Poster poster = posterRepository.findById(posterId).orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));
    return ResponseEntity.ok(convertToDTO(poster));
} catch (RuntimeException e) {
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

@Override
public ResponseEntity<?> getAllByUserId(UUID userId) {
try {
    var userDto = userClient.getUserById(userId);
    if (userDto == null) throw new RuntimeException("User không tồn tại với ID: " + userId);
    List<Poster> posters = posterRepository.findByUserOrderByCreatedAtDesc(userId);
    List<Object> posterDTOs = posters.stream().map(this::convertToDTO).collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(posterDTOs);
} catch (RuntimeException e) {
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

@Override
public ResponseEntity<?> getAll() {
try {
    List<Poster> posters = posterRepository.findAllByOrderByCreatedAtDesc();
    List<Object> posterDTOs = posters.stream().map(this::convertToDTO).collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(posterDTOs);
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

private Object convertToDTO(Poster poster) {
java.util.Map<String, Object> dto = new java.util.HashMap<>();
dto.put("idPoster", poster.getIdPoster());
dto.put("content", poster.getContent());
dto.put("createdAt", poster.getCreatedAt());
dto.put("updatedAt", poster.getUpdatedAt());

if (poster.getUser() != null) {
    try {
        var author = userClient.getUserById(poster.getUser());
        if (author != null) {
            dto.put("idUser", author.getIdUser());
            dto.put("userName", author.getUsername());
            dto.put("userFirstName", author.getFirstName());
            dto.put("userLastName", author.getLastName());
            dto.put("userAvatar", author.getAvatar());
        } else {
            dto.put("idUser", poster.getUser());
        }
    } catch (Exception ignored) {
        dto.put("idUser", poster.getUser());
    }
}

if (poster.getPrivacyStatus() != null) dto.put("privacyStatusName", poster.getPrivacyStatus().getName());

List<ImagePoster> images = imagePosterRepository.findByPoster(poster);
if (images != null && !images.isEmpty()) dto.put("imageUrls", images.stream().map(ImagePoster::getUrl).collect(java.util.stream.Collectors.toList()));

return dto;
}

@Override
public ResponseEntity<?> getAllVisibleToUser(UUID viewerId) {
try {
    log.info("🔍 Getting visible posters for viewer: {}", viewerId);
    var viewerDto = userClient.getUserById(viewerId);
    if (viewerDto == null) throw new RuntimeException("User không tồn tại với ID: " + viewerId);

    List<Poster> allPosters = posterRepository.findAllByOrderByCreatedAtDesc();
    log.info("📊 Total posters in database: {}", allPosters.size());

    List<Object> visiblePosters = allPosters.stream().filter(poster -> {
        String privacyName = poster.getPrivacyStatus() != null ? poster.getPrivacyStatus().getName() : "UNKNOWN";
        UUID authorId = poster.getUser();
        String authorName = "<unknown>";
        try {
            if (authorId != null) {
                var ad = userClient.getUserById(authorId);
                if (ad != null) authorName = ad.getUsername();
            }
        } catch (Exception ignored) {}

        log.debug("  🔹 Checking poster {} by {} with privacy: {}", poster.getIdPoster(), authorName, privacyName);

        if ("PUBLIC".equals(privacyName)) return true;
        if ("PRIVATE".equals(privacyName)) return authorId != null && authorId.equals(viewerId);
        if ("FRIENDS".equals(privacyName)) {
            if (authorId != null && authorId.equals(viewerId)) return true;
            return authorId != null && areFriends(viewerId, authorId);
        }
        return false;
    }).map(this::convertToDTO).collect(java.util.stream.Collectors.toList());

    log.info("✅ Returning {} visible posters", visiblePosters.size());
    return ResponseEntity.ok(visiblePosters);
} catch (RuntimeException e) {
    log.error("❌ Error: {}", e.getMessage());
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.badRequest().body("❌ Lỗi: " + e.getMessage());
}
}

private boolean areFriends(UUID userId1, UUID userId2) {
try {
    var friends = friendshipClient.getFriends(userId1);
    if (friends == null) return false;
    return friends.stream().anyMatch(u -> u.getIdUser().equals(userId2));
} catch (Exception e) {
    log.error("Error calling friendship-service: {}", e.getMessage());
    return false;
}
}
}
