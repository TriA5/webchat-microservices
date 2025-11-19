package poster_service.example.poster_service.exception;

/**
 * Exception được throw khi phát hiện ảnh chứa nội dung nhạy cảm (sexy/porn/hentai)
 */
public class InappropriateContentException extends RuntimeException {
    
    private final String contentType;
    private final double confidence;
    
    public InappropriateContentException(String contentType, double confidence) {
        super(String.format("Ảnh chứa nội dung %s (độ tin cậy: %.2f%%)", contentType, confidence * 100));
        this.contentType = contentType;
        this.confidence = confidence;
    }
    
    public InappropriateContentException(String message, String contentType, double confidence) {
        super(message);
        this.contentType = contentType;
        this.confidence = confidence;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public double getConfidence() {
        return confidence;
    }
}
