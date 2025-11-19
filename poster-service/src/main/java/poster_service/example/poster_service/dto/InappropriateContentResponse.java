package poster_service.example.poster_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để trả về response khi phát hiện nội dung nhạy cảm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InappropriateContentResponse {
    private boolean isInappropriate;
    private String contentType; // "sexy", "porn", hoặc "hentai"
    private double confidence;
    private String message;
    
    public static InappropriateContentResponse create(String contentType, double confidence) {
        String message = String.format("Ảnh chứa nội dung %s (độ tin cậy: %.2f%%)", contentType, confidence * 100);
        return new InappropriateContentResponse(true, contentType, confidence, message);
    }
}
