package ai_service.example.ai_service.service.Huggingface;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageCheckService {

    @Value("${huggingface.api.token}")
    private String HF_TOKEN;
    
    // Sử dụng Hugging Face Router API cho model LukeJacob2023/nsfw-image-detector
    private final String MODEL_URL = "https://router.huggingface.co/hf-inference/models/LukeJacob2023/nsfw-image-detector";

    /**
     * Kiểm tra ảnh có nội dung nhạy cảm tình dục hay không
     * @param imageBytes Dữ liệu ảnh dạng byte array
     * @return Kết quả phân loại với điểm số cho từng nhãn
     * @throws Exception Nếu có lỗi khi gọi API
     */
    public Map<String, Object> checkImage(byte[] imageBytes) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Gửi raw binary data với Content-Type: image/jpeg như trong Python example
        String response = Request.post(MODEL_URL)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("Content-Type", "image/jpeg")
                .bodyByteArray(imageBytes, ContentType.create("image/jpeg"))
                .execute()
                .returnContent()
                .asString();

        // Log response để debug
        System.out.println("HuggingFace Response: " + response);

        // Parse response
        JsonNode root = mapper.readTree(response);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (root.isArray() && root.size() > 0) {
                // Response format: [{"label": "sexy", "score": 0.99}, {"label": "porn", "score": 0.01}, ...]
                // Labels có thể là: sexy, porn, hentai, drawings, neutral
                
                double sexyScore = 0.0;
                double pornScore = 0.0;
                double hentaiScore = 0.0;
                double drawingsScore = 0.0;
                double neutralScore = 0.0;
                
                // Duyệt qua array predictions
                for (JsonNode prediction : root) {
                    JsonNode labelNode = prediction.get("label");
                    JsonNode scoreNode = prediction.get("score");
                    
                    if (labelNode != null && scoreNode != null) {
                        String label = labelNode.asText().toLowerCase();
                        double score = scoreNode.asDouble();
                        
                        switch (label) {
                            case "sexy":
                                sexyScore = score;
                                break;
                            case "porn":
                                pornScore = score;
                                break;
                            case "hentai":
                                hentaiScore = score;
                                break;
                            case "drawings":
                                drawingsScore = score;
                                break;
                            case "neutral":
                                neutralScore = score;
                                break;
                        }
                    }
                }
                
                // Tính tổng NSFW score (sexy + porn + hentai)
                double nsfwScore = sexyScore + pornScore + hentaiScore;
                double normalScore = neutralScore;
                
                // Tìm label có score cao nhất
                String topLabel = "neutral";
                double topScore = neutralScore;
                
                if (sexyScore > topScore) {
                    topLabel = "sexy";
                    topScore = sexyScore;
                }
                if (pornScore > topScore) {
                    topLabel = "porn";
                    topScore = pornScore;
                }
                if (hentaiScore > topScore) {
                    topLabel = "hentai";
                    topScore = hentaiScore;
                }
                if (drawingsScore > topScore) {
                    topLabel = "drawings";
                    topScore = drawingsScore;
                }
                
                // Kiểm tra nếu top label là porn, hentai, hoặc sexy
                boolean isNSFW = topLabel.equals("sexy") || topLabel.equals("porn") || topLabel.equals("hentai");
                boolean isSexy = topLabel.equals("sexy");
                
                result.put("sexy_score", sexyScore);
                result.put("porn_score", pornScore);
                result.put("hentai_score", hentaiScore);
                result.put("drawings_score", drawingsScore);
                result.put("neutral_score", neutralScore);
                result.put("nsfw_score", nsfwScore);
                result.put("normal_score", normalScore);
                result.put("top_label", topLabel);
                result.put("top_score", topScore);
                result.put("is_sexy", isSexy);
                result.put("is_nsfw", isNSFW);  // true nếu top label là sexy/porn/hentai
                result.put("confidence", topScore);
                result.put("classification", isNSFW ? topLabel.toUpperCase() : "NORMAL");
                result.put("model", "LukeJacob2023/nsfw-image-detector");
                
            } else {
                // Trường hợp response không như mong đợi
                result.put("error", "Unexpected response format");
                result.put("raw_response", response);
            }
        } catch (Exception e) {
            result.put("error", "Parse error: " + e.getMessage());
            result.put("raw_response", response);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Kiểm tra xem ảnh có phải NSFW không (trả về boolean đơn giản)
     */
    public boolean isNSFW(byte[] imageBytes) throws Exception {
        Map<String, Object> result = checkImage(imageBytes);
        return (Boolean) result.getOrDefault("is_nsfw", false);
    }
    
    /**
     * Kiểm tra xem ảnh có sexy/porn/hentai không (trả về boolean đơn giản)
     */
    public boolean isSexy(byte[] imageBytes) throws Exception {
        Map<String, Object> result = checkImage(imageBytes);
        // Trả về true nếu là sexy, porn, hoặc hentai
        return (Boolean) result.getOrDefault("is_nsfw", false);
    }
    
    /**
     * Lấy sexy score của ảnh
     */
    public double getSexyScore(byte[] imageBytes) throws Exception {
        Map<String, Object> result = checkImage(imageBytes);
        return (Double) result.getOrDefault("sexy_score", 0.0);
    }
}

