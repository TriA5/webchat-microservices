package user_service.example.user_service.service.UploadImage;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadImageImp implements UploadImageService{
     private final Cloudinary cloudinary;
    @Override
    public String uploadImage(MultipartFile multipartFile, String name) {
        String url = "";
        try{
             url = cloudinary.uploader()
                    .upload(multipartFile.getBytes(), Map.of("public_id", name))
                    .get("url")
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public void deleteImage(String imageUrl) {
        try{
            String publicId = getPublicIdImg(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception e) {
            System.out.println("Lỗi khi xoá ảnh");
            e.printStackTrace();
        }
    }

    private String getPublicIdImg(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String publicIdWithFormat = parts[parts.length - 1]; // Chỉ lấy phần cuối cùng của URL

        // Tách public_id và định dạng
        String[] publicIdAndFormat = publicIdWithFormat.split("\\.");
        return publicIdAndFormat[0]; // Lấy public_id
    }
}

