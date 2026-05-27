package aihub.backend.modelrequest;

import aihub.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRequestRepository extends JpaRepository<ModelRequest, Long> {
    Page<ModelRequest> findByStatus(ModelRequestStatus status, Pageable pageable);

    Page<ModelRequest> findBySubmitter(User submitter, Pageable pageable);

    long countByStatus(ModelRequestStatus status);
}
