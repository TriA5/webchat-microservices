package poster_service.example.poster_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "friendship-service")
public interface FriendshipClient {

    @GetMapping("/friendships/{userId}/friends")
    List<UserDTO> getFriends(@PathVariable("userId") UUID userId);
}
