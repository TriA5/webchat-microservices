package ai_service.example.ai_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.google.genai.Client;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Configuration
public class GeminiConfig {
    @Value("${google.genai.api-key}")
    private String apiKey;

    /**
     * WARNING: This disables SSL verification globally for the JVM.
     * ONLY USE IN DEVELOPMENT ENVIRONMENT!
     * DO NOT USE IN PRODUCTION!
     */
    @PostConstruct
    public void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("⚠️  SSL verification disabled - DEVELOPMENT MODE ONLY!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to disable SSL verification", e);
        }
    }

    @Bean
    public Client geminiClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
