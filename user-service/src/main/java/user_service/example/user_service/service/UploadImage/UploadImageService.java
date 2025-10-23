package user_service.example.user_service.service.UploadImage;

import org.springframework.web.multipart.MultipartFile;

public interface UploadImageService {
    String uploadImage(MultipartFile multipartFile, String name);
    void deleteImage(String imgUrl);
}

