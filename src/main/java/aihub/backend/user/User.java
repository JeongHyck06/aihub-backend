package aihub.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 500)
    private String refreshToken;

    public User(String email, String passwordHash, String name, UserRole role, boolean emailVerified) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.displayName = name;
        this.role = role;
        this.emailVerified = emailVerified;
    }
}
