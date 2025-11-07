package chat_service.example.chat_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple client that uploads multipart files to user-service /uploads/file
 * using RestTemplate. This is intended for local/dev use and forwards the
 * incoming Authorization header when present.
 */
@Component
public class UploadFileClient {
    private static final Logger log = LoggerFactory.getLogger(UploadFileClient.class);

    private final RestTemplate rest = new RestTemplate();

    public String uploadFile(MultipartFile file, String name) {
        try {
            String url = "http://user-service:8081/uploads/file";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // forward Authorization header if present on current request
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    if (request != null) {
                        String auth = request.getHeader("Authorization");
                        if (auth != null && !auth.isBlank()) {
                            headers.set("Authorization", auth);
                            log.info("[upload-file-client] Forwarding Authorization header present=true");
                        } else {
                            log.info("[upload-file-client] Forwarding Authorization header present=false");
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("[upload-file-client] Failed to read incoming request attributes: {}", ex.toString());
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(file));
            if (name != null) body.add("name", name);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("Failed to upload file to user-service: {}", e.toString(), e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    public org.springframework.core.io.ByteArrayResource downloadFile(String fileUrl, String authorizationHeader) {
        try {
            String url = "http://user-service:8081/uploads/download?fileUrl=" + 
                        java.net.URLEncoder.encode(fileUrl, java.nio.charset.StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                headers.set("Authorization", authorizationHeader);
                log.info("[upload-file-client] Forwarding Authorization header for download present=true");
            }

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> resp = rest.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
            
            byte[] data = resp.getBody();
            if (data == null) {
                throw new RuntimeException("Download returned null body");
            }
            
            return new org.springframework.core.io.ByteArrayResource(data);
        } catch (Exception e) {
            log.error("Failed to download file from user-service: {}", e.toString(), e);
            throw new RuntimeException("Download failed: " + e.getMessage(), e);
        }
    }

    /**
     * Wrap MultipartFile as a Spring Resource for RestTemplate multipart support
     */
    static class MultipartFileResource extends AbstractResource {
        private final MultipartFile multipartFile;

        MultipartFileResource(MultipartFile multipartFile) {
            this.multipartFile = multipartFile;
        }

        @Override
        public String getDescription() {
            return "MultipartFile resource - " + multipartFile.getOriginalFilename();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(multipartFile.getBytes());
        }

        @Override
        public String getFilename() {
            return multipartFile.getOriginalFilename();
        }
    }
}
