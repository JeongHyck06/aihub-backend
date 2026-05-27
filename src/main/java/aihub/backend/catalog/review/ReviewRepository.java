package aihub.backend.catalog.review;

import aihub.backend.catalog.AiService;
import aihub.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByService(AiService service, Pageable pageable);

    Page<Review> findByAuthor(User author, Pageable pageable);

    boolean existsByServiceAndAuthor(AiService service, User author);

    long countByService(AiService service);
}
