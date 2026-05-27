package aihub.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    List<NotificationSetting> findByUser(User user);

    Optional<NotificationSetting> findByUserAndType(User user, NotificationType type);
}
