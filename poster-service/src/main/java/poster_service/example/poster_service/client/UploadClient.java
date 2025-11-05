package poster_service.example.poster_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-service", contextId = "PosterUploadClient")
public interface UploadClient {
    // send JSON { "name": "...", "data": "data:image/...;base64,..." }
    @PostMapping("/uploads/base64")
    String uploadBase64(@RequestBody Map<String, String> body);

    // Upload multipart file
    // Upload multipart file
    @PostMapping(value = "/uploads/file", consumes = "multipart/form-data")
    String uploadFile(
        @RequestPart("file") MultipartFile file,
        @RequestPart(value = "name", required = false) String name
    );

    // Upload video base64
    @PostMapping("/uploads/video/base64")
    String uploadVideoBase64(@RequestBody Map<String, String> body);

    // Upload video multipart
    @PostMapping(value = "/uploads/video/file", consumes = "multipart/form-data")
    String uploadVideoFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "name", required = false) String name
    );

    // Delete uploaded image by full URL (calls user-service DELETE /uploads/delete?imageUrl=...)
    @DeleteMapping("/uploads/delete")
    String deleteByImageUrl(@RequestParam("imageUrl") String imageUrl);
    
    // Delete uploaded video by full URL
    @DeleteMapping("/uploads/video/delete")
    String deleteByVideoUrl(@RequestParam("videoUrl") String videoUrl);
}
 
