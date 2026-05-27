package aihub.backend.compare;

import aihub.backend.catalog.AiService;
import aihub.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "compare_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_compare_user_service", columnNames = {"user_id", "service_id"})
)
public class CompareItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private AiService service;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public CompareItem(User user, AiService service) {
        this.user = user;
        this.service = service;
        this.createdAt = Instant.now();
    }
}
