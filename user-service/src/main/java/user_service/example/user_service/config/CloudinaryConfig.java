package user_service.example.user_service.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class CloudinaryConfig {
    //    private final String CLOUD_NAME = "dhqu0tjno";
//    private final String API_KEY = "963355478491177";
//    private final String API_SECRET = "rQ4_pMlsNeUHtnONHdmnXUC-ES4";
    @Value("${cloud_name}")
    private String CLOUD_NAME;
    @Value("${api_key}")
    private String API_KEY;
    @Value("${api_secret}")
    private String API_SECRET;

    //    Config cloudinary (Nơi để chứa ảnh)
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        return new Cloudinary(config);
    }
}

