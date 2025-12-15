
package poster_service.example.poster_service.service.poster;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import poster_service.example.poster_service.client.UploadClient;
import poster_service.example.poster_service.client.AiClient;
import poster_service.example.poster_service.util.Base64ToMultipartFileConverter;
import poster_service.example.poster_service.entity.ImagePoster;
import poster_service.example.poster_service.exception.InappropriateContentException;
import java.util.Map;
import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.entity.PrivacyStatusPoster;
import poster_service.example.poster_service.entity.VideoPoster;
import poster_service.example.poster_service.repository.CommentPosterRepository;
import poster_service.example.poster_service.repository.ImagePosterRepository;
import poster_service.example.poster_service.repository.LikePosterRepository;
import poster_service.example.poster_service.repository.PosterRepository;
import poster_service.example.poster_service.repository.PrivacyStatusPosterRepository;
import poster_service.example.poster_service.repository.VideoPosterRepository;

@Service
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
private VideoPosterRepository videoPosterRepository;

@Autowired
private LikePosterRepository likePosterRepository;

@Autowired
private CommentPosterRepository commentPosterRepository;

@Autowired
private poster_service.example.poster_service.repository.SharePosterRepository sharePosterRepository;

@Autowired
private poster_service.example.poster_service.client.FriendshipClient friendshipClient;

@Autowired
private UploadClient uploadClient;

@Autowired
private AiClient aiClient;

private final ObjectMapper objectMapper;

public PosterServiceImpl(ObjectMapper objectMapper) {
this.objectMapper = objectMapper;
}

@Override
@Transactional
public ResponseEntity<?> save(JsonNode posterJson, UUID userId) {
try {
    log.info("📝 Creating poster with data: {}", posterJson);
    
    // Validate required fields
    if (posterJson == null) {
        log.error("❌ posterJson is null");
        return ResponseEntity.badRequest().body("❌ Dữ liệu poster không được để trống");
    }
    
    if (userId == null) {
        log.error("❌ userId is null");
        return ResponseEntity.badRequest().body("❌ User ID không được để trống");
    }
    
    if (!posterJson.has("content") || posterJson.get("content").asText().trim().isEmpty()) {
        log.error("❌ Content is empty");
        return ResponseEntity.badRequest().body("❌ Nội dung poster không được để trống");
    }
    
    if (!posterJson.has("privacyStatusName")) {
        log.error("❌ privacyStatusName is missing");
        return ResponseEntity.badRequest().body("❌ Trạng thái riêng tư không được để trống");
    }

    // Check user exists
    var userDto = userClient.getUserById(userId);
    if (userDto == null) {
        log.error("❌ User not found: {}", userId);
        throw new RuntimeException("User không tồn tại với ID: " + userId);
    }
    log.info("✅ User found: {}", userDto.getUsername());

    // Check privacy status exists
    String privacyStatusName = posterJson.get("privacyStatusName").asText();
    PrivacyStatusPoster privacyStatus = privacyStatusRepository.findByName(privacyStatusName)
            .orElseThrow(() -> {
                log.error("❌ Privacy status not found: {}", privacyStatusName);
                return new RuntimeException("Privacy status không tồn tại: " + privacyStatusName);
            });
    log.info("✅ Privacy status found: {}", privacyStatus.getName());

    // Create poster
    Poster poster = new Poster();
    poster.setContent(posterJson.get("content").asText());
    //Check toxic content
    if(isToxic(poster.getContent())) {
        log.error("❌ Poster content is toxic");
        return ResponseEntity.badRequest().body("❌ Nội dung poster chứa ngôn từ không phù hợp");
    }
    poster.setUser(userDto.getIdUser());
    poster.setPrivacyStatus(privacyStatus);
    poster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    poster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

    Poster newPoster = posterRepository.save(poster);
    log.info("✅ Poster created with ID: {}", newPoster.getIdPoster());

    // Process images
    if (posterJson.has("imageUrls") && posterJson.get("imageUrls").isArray()) {
        log.info("📷 Processing {} images", posterJson.get("imageUrls").size());
        List<String> imageList = objectMapper.readValue(posterJson.get("imageUrls").traverse(), new TypeReference<List<String>>() {});
        for (int i = 0; i < imageList.size(); i++) {
            String item = imageList.get(i);
            if (item != null && item.startsWith("data:")) {
                // 🔍 Kiểm tra ảnh có nội dung nhạy cảm TRƯỚC KHI upload
                log.info("🔍 Checking image {} for inappropriate content...", i);
                try {
                    validateImageContent(item);
                } catch (InappropriateContentException e) {
                    log.error("❌ Image {} contains inappropriate content: {}", i, e.getMessage());
                    // Xóa poster đã tạo nếu phát hiện ảnh không phù hợp
                    posterRepository.delete(newPoster);
                    throw e;
                }
                
                var body = java.util.Map.of("name", "poster_" + newPoster.getIdPoster() + "_" + i, "data", item);
                String imageUrl = null;
                try {
                    imageUrl = uploadClient.uploadBase64(body);
                } catch (Exception ex) {
                    log.error("❌ Failed to upload image {}: {}", i, ex.getMessage(), ex);
                }

                if (imageUrl != null && !imageUrl.isBlank()) {
                    ImagePoster imagePoster = new ImagePoster();
                    imagePoster.setPoster(newPoster);
                    imagePoster.setUrl(imageUrl);
                    imagePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    imagePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    imagePosterRepository.save(imagePoster);
                    log.info("✅ Image uploaded successfully: {}", imageUrl);
                } else {
                    log.error("❌ Upload returned null/empty URL for image {}", i);
                }
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

    // 🎬 Xử lý video upload
    if (posterJson.has("videoUrls") && posterJson.get("videoUrls").isArray()) {
        log.info("🎬 Processing {} videos", posterJson.get("videoUrls").size());
        List<String> videoList = objectMapper.readValue(posterJson.get("videoUrls").traverse(), new TypeReference<List<String>>() {});
        for (int i = 0; i < videoList.size(); i++) {
            String item = videoList.get(i);
            if (item != null && item.startsWith("data:video")) {
                try {
                    log.info("🎬 Uploading video {} to Cloudinary (base64 size: {} chars)", i, item.length());
                    MultipartFile file = Base64ToMultipartFileConverter.convert(item);
                    String videoUrl = uploadClient.uploadFile(file, "poster_video_" + newPoster.getIdPoster() + "_" + i);

                    if (videoUrl != null && !videoUrl.isBlank()) {
                        VideoPoster videoPoster = new VideoPoster();
                        videoPoster.setPoster(newPoster);
                        videoPoster.setUrl(videoUrl);
                        videoPoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                        videoPoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                        videoPosterRepository.save(videoPoster);
                        log.info("✅ Video uploaded successfully: {}", videoUrl);
                    } else {
                        log.error("❌ Upload returned null URL for video {}", i);
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to upload video {}: {}", i, e.getMessage(), e);
                    throw new RuntimeException("Lỗi upload video: " + e.getMessage(), e);
                }
            } else if (item != null && !item.isBlank() && (item.startsWith("http://") || item.startsWith("https://"))) {
                // Chỉ lưu URL nếu đã là link Cloudinary
                VideoPoster videoPoster = new VideoPoster();
                videoPoster.setPoster(newPoster);
                videoPoster.setUrl(item);
                videoPoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                videoPoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                videoPosterRepository.save(videoPoster);
                log.info("✅ Saved existing video URL: {}", item);
            } else {
                log.warn("⚠️ Skipping invalid video item at index {}", i);
            }
        }
    }

    log.info("🎉 Poster created successfully!");
    return ResponseEntity.ok("✅ Tạo poster thành công!");
} catch (InappropriateContentException e) {
    // Xử lý riêng cho ảnh nhạy cảm
    log.error("❌ Inappropriate content detected: {}", e.getMessage());
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (RuntimeException e) {
    log.error("❌ Runtime error: {}", e.getMessage(), e);
    return ResponseEntity.badRequest().body("❌ " + e.getMessage());
} catch (Exception e) {
    log.error("❌ Unexpected error: {}", e.getMessage(), e);
    e.printStackTrace();
    return ResponseEntity.status(500).body("❌ Lỗi hệ thống: " + e.getMessage());
}
}

@Override
@Transactional
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

        // Xóa ảnh cũ không còn trong danh sách mới
        for (ImagePoster imageToDelete : imagesToDelete) {
            try {
                // Xóa trên Cloudinary nếu là URL Cloudinary
                if (imageToDelete.getUrl() != null && imageToDelete.getUrl().contains("cloudinary")) {
                    try {
                        uploadClient.deleteByImageUrl(imageToDelete.getUrl());
                        log.info("🗑️ Deleted old image from Cloudinary: {}", imageToDelete.getUrl());
                    } catch (Exception ex) {
                        log.error("❌ Failed to delete image from Cloudinary: {}", ex.getMessage());
                    }
                }
                // Xóa record trong database
                imagePosterRepository.delete(imageToDelete);
            } catch (Exception e) {
                log.error("❌ Lỗi khi xóa ảnh: {}", e.getMessage());
            }
        }

        // Thêm ảnh mới (base64) hoặc giữ lại ảnh cũ (URL Cloudinary)
        for (String imageUrl : imageList) {
            if (imageUrl != null && imageUrl.startsWith("data:")) {
                var body = java.util.Map.of("name", "poster_" + poster.getIdPoster() + "_" + System.currentTimeMillis(), "data", imageUrl);
                String uploadedUrl = null;
                try {
                    uploadedUrl = uploadClient.uploadBase64(body);
                } catch (Exception ex) {
                    log.error("❌ Failed to upload image during update: {}", ex.getMessage(), ex);
                }

                if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                    ImagePoster imagePoster = new ImagePoster();
                    imagePoster.setPoster(poster);
                    imagePoster.setUrl(uploadedUrl);
                    imagePoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    imagePoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    imagePosterRepository.save(imagePoster);
                    log.info("✅ Image updated successfully: {}", uploadedUrl);
                } else {
                    log.error("❌ Upload returned null/empty URL when updating image for poster {}", poster.getIdPoster());
                }
            }
            // Nếu là URL Cloudinary cũ thì không cần làm gì (đã có trong DB)
        }
    }

    // 🎬 Xử lý cập nhật video
    if (posterJson.has("videoUrls") && posterJson.get("videoUrls").isArray()) {
        List<String> videoList = objectMapper.readValue(posterJson.get("videoUrls").traverse(), new TypeReference<List<String>>() {});
        List<VideoPoster> oldVideos = videoPosterRepository.findByPoster(poster);
        List<VideoPoster> videosToDelete = oldVideos.stream().filter(vid -> !videoList.contains(vid.getUrl())).collect(java.util.stream.Collectors.toList());

        // Xóa video cũ không còn trong danh sách mới
        for (VideoPoster videoToDelete : videosToDelete) {
            try {
                // Xóa trên Cloudinary nếu là URL Cloudinary
                if (videoToDelete.getUrl() != null && videoToDelete.getUrl().contains("cloudinary")) {
                    try {
                        uploadClient.deleteByVideoUrl(videoToDelete.getUrl());
                        log.info("🗑️ Deleted old video from Cloudinary: {}", videoToDelete.getUrl());
                    } catch (Exception ex) {
                        log.error("❌ Failed to delete video from Cloudinary: {}", ex.getMessage());
                    }
                }
                // Xóa record trong database
                videoPosterRepository.delete(videoToDelete);
            } catch (Exception e) {
                log.error("❌ Lỗi khi xóa video: {}", e.getMessage());
            }
        }

        // Thêm video mới (base64) hoặc giữ lại video cũ (URL Cloudinary)
        for (String videoUrl : videoList) {
            if (videoUrl != null && videoUrl.startsWith("data:video")) {
                try {
                    MultipartFile file = Base64ToMultipartFileConverter.convert(videoUrl);
                    String uploadedUrl = uploadClient.uploadVideoFile(file, "poster_video_" + poster.getIdPoster() + "_" + System.currentTimeMillis());

                    if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                        VideoPoster videoPoster = new VideoPoster();
                        videoPoster.setPoster(poster);
                        videoPoster.setUrl(uploadedUrl);
                        videoPoster.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                        videoPoster.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                        videoPosterRepository.save(videoPoster);
                        log.info("✅ Video updated successfully: {}", uploadedUrl);
                    } else {
                        log.error("❌ Upload returned null URL for video");
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to upload video: {}", e.getMessage());
                }
            }
            // Nếu là URL Cloudinary cũ thì không cần làm gì (đã có trong DB)
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
@Transactional
public ResponseEntity<?> delete(UUID posterId, UUID userId) {
try {
    Poster poster = posterRepository.findById(posterId).orElseThrow(() -> new RuntimeException("Poster không tồn tại với ID: " + posterId));
    if (!poster.getUser().equals(userId)) return ResponseEntity.status(403).body("❌ Bạn không có quyền xóa poster này!");

    // Xóa các quan hệ phụ thuộc trước khi xóa poster để tránh vi phạm ràng buộc khóa ngoại
    commentPosterRepository.deleteAllByPosterId(posterId);
    likePosterRepository.deleteAllByPosterId(posterId);
    
    // 📤 Xóa tất cả share của poster này
    sharePosterRepository.deleteAllByPosterId(posterId);

    // Xóa images
    List<ImagePoster> images = imagePosterRepository.findByPoster(poster);
    for (ImagePoster image : images) {
        try {
            uploadClient.deleteByImageUrl(image.getUrl());
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
        }
    }

    // 🎬 Xóa videos
    List<VideoPoster> videos = videoPosterRepository.findByPoster(poster);
    for (VideoPoster video : videos) {
        try {
            uploadClient.deleteByVideoUrl(video.getUrl());
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa video: " + e.getMessage());
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

// 📷 Images
List<ImagePoster> images = imagePosterRepository.findByPoster(poster);
if (images != null && !images.isEmpty()) {
    dto.put("imageUrls", images.stream().map(ImagePoster::getUrl).collect(java.util.stream.Collectors.toList()));
}

// 🎬 Videos
List<VideoPoster> videos = videoPosterRepository.findByPoster(poster);
if (videos != null && !videos.isEmpty()) {
    List<java.util.Map<String, Object>> videoData = videos.stream().map(video -> {
        java.util.Map<String, Object> videoInfo = new java.util.HashMap<>();
        videoInfo.put("url", video.getUrl());
        videoInfo.put("thumbnailUrl", video.getThumbnailUrl());
        videoInfo.put("duration", video.getDuration());
        videoInfo.put("fileSize", video.getFileSize());
        return videoInfo;
    }).collect(java.util.stream.Collectors.toList());
    dto.put("videos", videoData);
}

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

/**
 * Kiểm tra ảnh có chứa nội dung nhạy cảm (sexy/porn/hentai) hay không
 * @param base64Image Ảnh dạng base64
 * @throws InappropriateContentException nếu phát hiện nội dung không phù hợp
 */
private void validateImageContent(String base64Image) {
    try {
        log.info("🔍 Calling AI service to check image content...");
        
        // Loại bỏ prefix "data:image/...;base64," nếu có
        String cleanBase64 = base64Image;
        if (base64Image.contains(",")) {
            cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        
        // Gọi AI service để kiểm tra - QUAN TRỌNG: field phải là "image" không phải "data"
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("image", cleanBase64);
        
        log.info("🔍 Sending request to AI service with image data length: {}", cleanBase64.length());
        Map<String, Object> response = aiClient.checkImageSexyBase64(body);
        
        log.info("🤖 AI Response: {}", response);
        
        // Kiểm tra kết quả
        Boolean isSexy = (Boolean) response.get("is_sexy");
        
        if (isSexy != null && isSexy) {
            // Lấy điểm số các loại nội dung
            Double pornScore = getDoubleValue(response.get("porn_score"));
            Double sexyScore = getDoubleValue(response.get("sexy_score"));
            Double hentaiScore = getDoubleValue(response.get("hentai_score"));
            Double confidence = getDoubleValue(response.get("confidence"));
            
            // Xác định loại nội dung có điểm cao nhất
            String contentType;
            double maxScore;
            
            if (pornScore > sexyScore && pornScore > hentaiScore) {
                contentType = "porn";
                maxScore = pornScore;
            } else if (hentaiScore > sexyScore && hentaiScore > pornScore) {
                contentType = "hentai";
                maxScore = hentaiScore;
            } else {
                contentType = "sexy";
                maxScore = sexyScore;
            }
            
            log.error("❌ Inappropriate content detected - Type: {}, Score: {}, Confidence: {}", 
                     contentType, maxScore, confidence);
            
            throw new InappropriateContentException(contentType, confidence != null ? confidence : maxScore);
        }
        
        log.info("✅ Image content is appropriate");
        
    } catch (InappropriateContentException e) {
        throw e; // Re-throw để xử lý ở layer trên
    } catch (Exception e) {
        log.error("⚠️ Failed to check image content, allowing by default: {}", e.getMessage());
        // Nếu service AI không hoạt động, cho phép upload (hoặc có thể reject tùy yêu cầu)
    }
}

/**
 * Helper method để convert Object sang Double
 */
private Double getDoubleValue(Object value) {
    if (value == null) return 0.0;
    if (value instanceof Double) return (Double) value;
    if (value instanceof Number) return ((Number) value).doubleValue();
    if (value instanceof String) {
        try {
            return Double.parseDouble((String) value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    return 0.0;
}
private boolean isToxic(String content) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", content);
            
            Map<String, Object> response = aiClient.checkToxic(body);
            
            // Kiểm tra kết quả từ AI service
            if (response != null && response.containsKey("toxic")) {
                return (Boolean) response.get("toxic");
            }
            
            // Nếu không có response hoặc lỗi, mặc định cho phép gửi tin nhắn
            return false;
        } catch (Exception e) {
            log.warn("Không thể kiểm tra toxic, cho phép tin nhắn: " + e.getMessage());
            // Nếu AI service không khả dụng, vẫn cho phép gửi tin nhắn
            return false;
        }
    }
}
