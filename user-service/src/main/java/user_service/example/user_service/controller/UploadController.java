package user_service.example.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.multipart.MultipartFile;

import user_service.example.user_service.service.UploadImage.UploadImageService;
import user_service.example.user_service.service.UploadImage.FileUploadService;
import user_service.example.user_service.service.util.Base64ToMultipartFileConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/uploads")
public class UploadController {

    @Autowired
    private UploadImageService uploadImageService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private Cloudinary cloudinary;

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

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam(name = "fileUrl") String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return ResponseEntity.badRequest().body("missing fileUrl");
            }

            String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
            PublicAssetDescriptor descriptor = resolvePublicId(decodedUrl);
            if (descriptor == null) {
                return ResponseEntity.badRequest().body("invalid cloudinary fileUrl");
            }

            ensurePublicAccess(descriptor);

            String downloadUrl = buildDownloadUrl(decodedUrl, descriptor);

            HttpURLConnection connection = null;
            byte[] fileBytes;
            try {
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(30_000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    logErrorStream(connection);
                    System.out.println("❌ Cloudinary download failed with status: " + responseCode);
                    return ResponseEntity.status(responseCode).body("failed to download file from cloudinary");
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    fileBytes = StreamUtils.copyToByteArray(inputStream);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            String pathSegment = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
            String filename = pathSegment.contains("?") ? pathSegment.substring(0, pathSegment.indexOf('?')) : pathSegment;

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.setContentLength(fileBytes.length);

            ByteArrayResource resource = new ByteArrayResource(fileBytes);
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("download failed");
        }
    }

    private void ensurePublicAccess(PublicAssetDescriptor descriptor) {
        try {
            Map resourceDetails = cloudinary.api().resource(
                descriptor.publicId,
                ObjectUtils.asMap(
                    "resource_type", descriptor.resourceType,
                    "type", "upload"
                )
            );

            String accessMode = resourceDetails != null ? (String) resourceDetails.get("access_mode") : null;
            if ("public".equals(accessMode)) {
                System.out.println("✅ Cloudinary asset already public: " + descriptor.publicId);
                return;
            }

            System.out.println(
                "🔄 Updating Cloudinary asset to public: " + descriptor.publicId + " (current access_mode=" + accessMode + ")");

            Map updateResult = cloudinary.api().update(
                descriptor.publicId,
                ObjectUtils.asMap(
                    "resource_type", descriptor.resourceType,
                    "type", "upload",
                    "access_mode", "public",
                    "invalidate", true
                )
            );

            System.out.println("✅ Cloudinary update response: " + updateResult);
        } catch (Exception ex) {
            System.out.println("⚠️ Unable to update Cloudinary access mode: " + ex.getMessage());
        }
    }

    private String buildDownloadUrl(String originalUrl, PublicAssetDescriptor descriptor) {
        if (descriptor.format == null || descriptor.format.isBlank()) {
            return originalUrl;
        }

        try {
            String publicIdWithFormat = descriptor.publicId + "." + descriptor.format;
            Url urlBuilder = cloudinary.url()
                .resourceType(descriptor.resourceType)
                .type("upload")
                .secure(true)
                .signed(true);

            if (descriptor.version != null && !descriptor.version.isBlank()) {
                urlBuilder = urlBuilder.version(descriptor.version);
            }

            String signedUrl = urlBuilder.generate(publicIdWithFormat);

            if (signedUrl != null && !signedUrl.isBlank()) {
                System.out.println("🔐 Using signed Cloudinary URL for download: " + signedUrl);
                return signedUrl;
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Failed to build signed Cloudinary URL, falling back to original: " + ex.getMessage());
        }

        return originalUrl;
    }

    private void logErrorStream(HttpURLConnection connection) {
        try (InputStream errorStream = connection.getErrorStream()) {
            if (errorStream == null) {
                return;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            errorStream.transferTo(buffer);
            String responseBody = buffer.toString(StandardCharsets.UTF_8);
            if (!responseBody.isBlank()) {
                System.out.println("Cloudinary error body: " + responseBody);
            }
        } catch (IOException ioException) {
            System.out.println("⚠️ Unable to read Cloudinary error stream: " + ioException.getMessage());
        }
    }

    private PublicAssetDescriptor resolvePublicId(String decodedUrl) {
        int uploadIndex = decodedUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String resourceType = detectResourceType(decodedUrl);
        String afterUpload = decodedUrl.substring(uploadIndex + "/upload/".length());

        int queryIndex = afterUpload.indexOf('?');
        if (queryIndex != -1) {
            afterUpload = afterUpload.substring(0, queryIndex);
        }

        String[] segments = afterUpload.split("/");
        int startIndex = 0;
        String version = null;
        if (segments.length > 0 && segments[0].startsWith("v") && segments[0].length() > 1 && Character.isDigit(segments[0].charAt(1))) {
            startIndex = 1;
            version = segments[0].substring(1);
        }

        if (startIndex >= segments.length) {
            return null;
        }

        StringBuilder publicIdBuilder = new StringBuilder();
        for (int i = startIndex; i < segments.length; i++) {
            if (i > startIndex) {
                publicIdBuilder.append('/');
            }
            publicIdBuilder.append(segments[i]);
        }

        String publicIdWithFormat = publicIdBuilder.toString();
        int dotIndex = publicIdWithFormat.lastIndexOf('.');
        String format = dotIndex > 0 ? publicIdWithFormat.substring(dotIndex + 1) : null;
        String publicId = dotIndex > 0 ? publicIdWithFormat.substring(0, dotIndex) : publicIdWithFormat;

        return new PublicAssetDescriptor(publicId, resourceType, format, version);
    }

    private String detectResourceType(String fileUrl) {
        if (fileUrl.contains("/video/upload/")) {
            return "video";
        }
        if (fileUrl.contains("/raw/upload/")) {
            return "raw";
        }
        return "image";
    }

    private static final class PublicAssetDescriptor {
        private final String publicId;
        private final String resourceType;
        private final String format;
        private final String version;

        private PublicAssetDescriptor(String publicId, String resourceType, String format, String version) {
            this.publicId = publicId;
            this.resourceType = resourceType;
            this.format = format;
            this.version = version;
        }
    }
}
