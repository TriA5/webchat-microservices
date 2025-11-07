package user_service.example.user_service.service.UploadImage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImp implements FileUploadService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile multipartFile, String name) {
        String url = "";
        try {
            // Cloudinary tự động xác định resource_type dựa trên file
            String contentType = multipartFile.getContentType();
            String resourceType = "raw";
            if (contentType != null) {
                if (contentType.startsWith("image/")) resourceType = "image";
                else if (contentType.startsWith("video/")) resourceType = "video";
                else resourceType = "raw";
            }
            // Ensure uploaded file is public and set explicit resource_type when needed
            url = cloudinary.uploader()
                    .upload(multipartFile.getBytes(), 
                        Map.of(
                            "public_id", name,
                            "resource_type", resourceType,
                            "access_mode", "public"
                        ))
                    .get("url")
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
        return url;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String publicId = getPublicId(fileUrl);
            String resourceType = detectResourceType(fileUrl);
            
            System.out.println("🗑️ Deleting file from Cloudinary: " + publicId + " (type: " + resourceType + ")");
            
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, 
                ObjectUtils.asMap("resource_type", resourceType));
            
            System.out.println("✅ Delete result: " + result);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi xóa file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String detectResourceType(String fileUrl) {
        // Cloudinary URL format: http://res.cloudinary.com/.../image/upload/... hoặc /video/upload/...
        if (fileUrl.contains("/video/upload/")) {
            return "video";
        } else if (fileUrl.contains("/image/upload/")) {
            return "image";
        } else if (fileUrl.contains("/raw/upload/")) {
            return "raw";
        }
        // Default to auto for unknown types
        return "image";
    }

    private String getPublicId(String fileUrl) {
        String[] parts = fileUrl.split("/");
        String publicIdWithFormat = parts[parts.length - 1];
        String[] publicIdAndFormat = publicIdWithFormat.split("\\.");
        return publicIdAndFormat[0];
    }
}

