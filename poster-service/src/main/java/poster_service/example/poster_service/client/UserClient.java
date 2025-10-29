package poster_service.example.poster_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/users/search")
    UserDTO searchByPhone(@RequestParam("phone") String phone);
}

