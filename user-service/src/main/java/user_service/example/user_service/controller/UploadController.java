package user_service.example.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.multipart.MultipartFile;

import user_service.example.user_service.service.UploadImage.UploadImageService;
import user_service.example.user_service.service.UploadImage.FileUploadService;
import user_service.example.user_service.service.util.Base64ToMultipartFileConverter;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/uploads")
public class UploadController {

    @Autowired
    private UploadImageService uploadImageService;

    @Autowired
    private FileUploadService fileUploadService;

    // Accept base64 JSON: { "name": "...", "data": "data:image/...;base64,...." }
    @PostMapping("/base64")
    public ResponseEntity<?> uploadBase64(@RequestBody ObjectNode body) {
        try {
            String name = body.has("name") ? body.get("name").asText() : "upload";
            String data = body.has("data") ? body.get("data").asText() : null;
            if (data == null) return ResponseEntity.badRequest().body("missing data");
            MultipartFile file = Base64ToMultipartFileConverter.convert(data);
            String url = uploadImageService.uploadImage(file, name);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("upload failed");
        }
    }
    // Accept multipart/form-data file upload
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(name = "name", required = false) String name) {
        try {
            if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("missing file");
            String finalName = (name == null || name.isBlank()) ? file.getOriginalFilename() : name;
            String url = fileUploadService.uploadFile(file, finalName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("upload failed");
        }
    }

    // Accept base64 encoded video payload
    @PostMapping("/video/base64")
    public ResponseEntity<?> uploadVideoBase64(@RequestBody ObjectNode body) {
        try {
            String name = body.has("name") ? body.get("name").asText() : "video";
            String data = body.has("data") ? body.get("data").asText() : null;
            if (data == null) return ResponseEntity.badRequest().body("missing data");
            MultipartFile file = Base64ToMultipartFileConverter.convert(data);
            String url = fileUploadService.uploadFile(file, name);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("upload failed");
        }
    }

    // Accept multipart/form-data video upload
    @PostMapping("/video/file")
    public ResponseEntity<?> uploadVideoFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam(name = "name", required = false) String name) {
        try {
            if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("missing file");
            String finalName = (name == null || name.isBlank()) ? file.getOriginalFilename() : name;
            String url = fileUploadService.uploadFile(file, finalName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("upload failed");
        }
    }

    // Delete an uploaded image by its full URL (other services can call this)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUploadImage(@RequestParam(name = "imageUrl") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank()) return ResponseEntity.badRequest().body("missing imageUrl");
            uploadImageService.deleteImage(imageUrl);
            return ResponseEntity.ok("deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("delete failed");
        }
    }
    
    // Delete uploaded video (or any file) by URL
    @DeleteMapping("/video/delete")
    public ResponseEntity<?> deleteUploadVideo(@RequestParam(name = "videoUrl") String videoUrl) {
        try {
            if (videoUrl == null || videoUrl.isBlank()) return ResponseEntity.badRequest().body("missing videoUrl");
            fileUploadService.deleteFile(videoUrl);
            return ResponseEntity.ok("deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("delete failed");
        }
    }
};
