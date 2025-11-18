package chat_service.example.chat_service.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "ai-service", contextId = "HuggingfaceClient")
public interface ImageCheckFeignClient {

    /**
     * Kiểm tra ảnh MultipartFile
     */
    @PostMapping(value = "/huggingface/check-image", consumes = "multipart/form-data")
    Map<String, Object> checkImage(@RequestPart("file") MultipartFile file);

    /**
     * Kiểm tra ảnh Base64
     */
    @PostMapping("/huggingface/check-image-base64")
    Map<String, Object> checkImageBase64(@RequestBody Map<String, Object> body);

    /**
     * Kiểm tra ảnh NSFW (MultipartFile)
     */
    @PostMapping(value = "/huggingface/is-nsfw", consumes = "multipart/form-data")
    Map<String, Object> isNSFW(@RequestPart("file") MultipartFile file);

    /**
     * Kiểm tra ảnh sexy/porn/hentai (MultipartFile)
     */
    @PostMapping(value = "/huggingface/is-image-sexy", consumes = "multipart/form-data")
    Map<String, Object> isSexy(@RequestPart("file") MultipartFile file);

    /**
     * Kiểm tra ảnh sexy/porn/hentai (Base64)
     */
    @PostMapping("/huggingface/is-image-sexy-base64")
    Map<String, Object> isSexyBase64(@RequestBody Map<String, Object> body);
}

