package aihub.backend.compare;

import aihub.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompareItemRepository extends JpaRepository<CompareItem, Long> {
    List<CompareItem> findByUserOrderByCreatedAtDesc(User user);

    @Query("select c from CompareItem c where c.user = :user and c.service.slug = :slug")
    Optional<CompareItem> findByUserAndServiceSlug(@Param("user") User user, @Param("slug") String slug);

    long countByUser(User user);
}
