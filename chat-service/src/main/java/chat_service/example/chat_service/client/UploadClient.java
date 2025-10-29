package chat_service.example.chat_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "user-service", contextId = "chatUploadClient")
public interface UploadClient {
    // send JSON { "name": "...", "data": "data:image/...;base64,..." }
    @PostMapping("/uploads/base64")
    String uploadBase64(@RequestBody Map<String, String> body);

    // Upload multipart file
    @PostMapping(value = "/uploads/file", consumes = "multipart/form-data")
    String uploadFile(
        @RequestPart("file") MultipartFile file,
        @RequestPart(value = "name", required = false) String name
    );
}
 