package chat_service.example.chat_service.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_video_call_participants")
@Data
@NoArgsConstructor
public class GroupVideoCallParticipants {

    @EmbeddedId
    private GroupVideoCallParticipantsId id;

    private LocalDateTime joinedAt = LocalDateTime.now();
}
