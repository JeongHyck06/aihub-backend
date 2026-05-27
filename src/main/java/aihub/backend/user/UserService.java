package aihub.backend.user;

import aihub.backend.catalog.review.ReviewRepository;
import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final ReviewRepository reviewRepository;

    public UserService(
            UserRepository userRepository,
            NotificationSettingRepository notificationSettingRepository,
            ReviewRepository reviewRepository
    ) {
        this.userRepository = userRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse me(CurrentUser currentUser) {
        User user = loadUser(currentUser);
        long reviews = reviewRepository.findByAuthor(user, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        return MeResponse.from(user, MeResponse.UserStats.empty(reviews));
    }

    @Transactional
    public MeResponse updateMe(CurrentUser currentUser, UpdateProfileRequest request) {
        User user = loadUser(currentUser);
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        long reviews = reviewRepository.findByAuthor(user, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        return MeResponse.from(user, MeResponse.UserStats.empty(reviews));
    }

    @Transactional(readOnly = true)
    public List<NotificationSettingResponse> notifications(CurrentUser currentUser) {
        User user = loadUser(currentUser);
        Map<NotificationType, Boolean> values = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            values.put(type, true);
        }
        notificationSettingRepository.findByUser(user).forEach(setting ->
                values.put(setting.getType(), setting.isEnabled())
        );
        return List.of(
                NotificationSettingResponse.of(NotificationType.REVIEW_COMMENT, values.get(NotificationType.REVIEW_COMMENT)),
                NotificationSettingResponse.of(NotificationType.NEW_FOLLOWER, values.get(NotificationType.NEW_FOLLOWER))
        );
    }

    @Transactional
    public List<NotificationSettingResponse> updateNotifications(
            CurrentUser currentUser,
            UpdateNotificationSettingsRequest request
    ) {
        User user = loadUser(currentUser);
        request.settings().forEach(update -> {
            NotificationSetting setting = notificationSettingRepository
                    .findByUserAndType(user, update.type())
                    .orElseGet(() -> new NotificationSetting(user, update.type(), update.enabled()));
            setting.setEnabled(update.enabled());
            notificationSettingRepository.save(setting);
        });
        return notifications(currentUser);
    }

    private User loadUser(CurrentUser currentUser) {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
    }
}
