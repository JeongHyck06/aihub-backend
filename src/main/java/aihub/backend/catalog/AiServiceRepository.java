package aihub.backend.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiServiceRepository extends JpaRepository<AiService, Long> {
    boolean existsBySlug(String slug);

    Optional<AiService> findBySlug(String slug);

    @Query("""
            select distinct s
            from AiService s
            left join s.tags t
            where s.status = 'PUBLISHED'
              and (:query is null or :query = ''
                or lower(s.name) like lower(concat('%', :query, '%'))
                or lower(s.provider) like lower(concat('%', :query, '%'))
                or lower(s.description) like lower(concat('%', :query, '%'))
                or lower(t) like lower(concat('%', :query, '%')))
              and (:categorySlug is null or s.category.slug = :categorySlug)
            """)
    Page<AiService> search(
            @Param("query") String query,
            @Param("categorySlug") String categorySlug,
            Pageable pageable
    );
}
