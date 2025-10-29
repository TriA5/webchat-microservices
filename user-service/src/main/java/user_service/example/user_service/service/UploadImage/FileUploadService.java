package user_service.example.user_service.service.UploadImage;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadFile(MultipartFile multipartFile, String name);
    void deleteFile(String fileUrl);
}

