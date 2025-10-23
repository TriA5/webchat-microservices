package chat_service.example.chat_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVideoCallParticipantsId implements Serializable {

    @Column(name = "call_id", length = 255, nullable = false, columnDefinition = "VARCHAR(255)")
    private String callId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;
}
