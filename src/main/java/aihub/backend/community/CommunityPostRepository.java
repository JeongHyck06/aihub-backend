package aihub.backend.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("""
            select p from CommunityPost p
            where p.deletedAt is null
              and (:category is null or p.category = :category)
            """)
    Page<CommunityPost> search(@Param("category") CommunityCategory category, Pageable pageable);
}
