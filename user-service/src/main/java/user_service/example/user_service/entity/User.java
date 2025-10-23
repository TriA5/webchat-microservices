package user_service.example.user_service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_user", updatable = false, nullable = false)
    private UUID idUser;

    @Column(name = "user_name", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", length = 150)
    private String firstName;

    @Column(name = "last_name", length = 150)
    private String lastName;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "date_of_birth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth; // yyyy-MM-dd

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "status")
    private boolean status;

    @Column(name = "enabled")
    private boolean enabled;
    //giới tính
    @Column(name = "gender")
    private boolean gender; // true = Nam, false = Nữ

    // Mã kích hoạt
    @Column(name = "activation_code")
    private String activationCode;

    // Thời điểm tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Thời điểm cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "id_user"), inverseJoinColumns = @JoinColumn(name = "id_role"))
    @JsonManagedReference
    private List<Role> listRoles; // Danh sách role của user

    //
    public boolean getGender() {
        return gender;
    }
    //
}

