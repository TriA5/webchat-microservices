package poster_service.example.poster_service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    private String content;
    private UUID userId;
    private UUID parentCommentId; // null nếu là comment gốc, có giá trị nếu là reply
}
