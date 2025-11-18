package ai_service.example.ai_service.controller;

import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ai_service.example.ai_service.service.Huggingface.ImageCheckService;


@RestController
@RequestMapping("/huggingface")
// @CrossOrigin(origins = "*") // Cho phép CORS từ mọi nguồn
public class ImageCheckController {

    private final ImageCheckService imageCheckService;

    public ImageCheckController(ImageCheckService imageCheckService) {
        this.imageCheckService = imageCheckService;
    }

    /**
     * Endpoint kiểm tra ảnh bằng MultipartFile
     * POST /api/image/check
     */
    @PostMapping("/check-image")
    public ResponseEntity<?> checkImageFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File phải là định dạng ảnh"));
            }

            Map<String, Object> result = imageCheckService.checkImage(file.getBytes());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint kiểm tra ảnh bằng Base64
     * POST /api/image/check-base64
     * Body: { "inputs": "base64_string" } hoặc { "inputs": { "image": "base64_string" } }
     */
    @PostMapping("/check-image-base64")
    public ResponseEntity<?> checkImageBase64(@RequestBody Map<String, Object> body) {
        try {
            Object inputs = body.get("inputs");
            if (inputs == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "inputs không được để trống"));
            }

            String base64;
            if (inputs instanceof String) {
                base64 = (String) inputs;
            } else if (inputs instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inputMap = (Map<String, Object>) inputs;
                base64 = (String) inputMap.get("image");
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "inputs không hợp lệ"));
            }

            if (base64 == null || base64.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Base64 string không được để trống"));
            }

            // Loại bỏ prefix "data:image/...;base64," nếu có
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64);
            Map<String, Object> result = imageCheckService.checkImage(imageBytes);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Base64 string không hợp lệ"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint đơn giản chỉ trả về true/false
     * POST /api/image/is-nsfw
     */
    @PostMapping("/is-nsfw")
    public ResponseEntity<?> isNSFW(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            boolean isNSFW = imageCheckService.isNSFW(file.getBytes());
            return ResponseEntity.ok(Map.of(
                "is_nsfw", isNSFW,
                "message", isNSFW ? "Ảnh có nội dung nhạy cảm" : "Ảnh an toàn"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Endpoint kiểm tra ảnh có sexy/porn/hentai không
     * POST /api/image/is-sexy
     */
    @PostMapping("/is-image-sexy")
    public ResponseEntity<?> isSexy(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            Map<String, Object> fullResult = imageCheckService.checkImage(file.getBytes());
            boolean isSexy = imageCheckService.isSexy(file.getBytes());
            
            return ResponseEntity.ok(Map.of(
                "is_sexy", isSexy,
                "top_label", fullResult.get("top_label"),
                "sexy_score", fullResult.get("sexy_score"),
                "porn_score", fullResult.get("porn_score"),
                "hentai_score", fullResult.get("hentai_score"),
                "confidence", fullResult.get("confidence"),
                "message", isSexy ? "Ảnh có nội dung nhạy cảm (sexy/porn/hentai)" : "Ảnh bình thường"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/is-image-sexy-base64")
    public ResponseEntity<?> isSexyBase64(@RequestBody Map<String, Object> body) {
        try {
            Object imageInput = body.get("image");
            if (imageInput == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "image không được để trống"));
            }

            String base64 = imageInput.toString();
            
            if (base64.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Base64 string không được để trống"));
            }

            // Loại bỏ prefix "data:image/...;base64," nếu có
            if (base64.contains(",")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64);
            Map<String, Object> fullResult = imageCheckService.checkImage(imageBytes);
            boolean isSexy = imageCheckService.isSexy(imageBytes);
            
            return ResponseEntity.ok(Map.of(
                "is_sexy", isSexy,
                "top_label", fullResult.get("top_label"),
                "sexy_score", fullResult.get("sexy_score"),
                "porn_score", fullResult.get("porn_score"),
                "hentai_score", fullResult.get("hentai_score"),
                "confidence", fullResult.get("confidence"),
                "message", isSexy ? "Ảnh có nội dung nhạy cảm (sexy/porn/hentai)" : "Ảnh bình thường"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Base64 string không hợp lệ"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

