package poster_service.example.poster_service.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private UUID idComment;
    private String content;
    private UUID idUser;
    private UUID idPoster;
    private UUID parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<CommentDTO> replies = new ArrayList<>();
    private int replyCount;
}
