package aihub.backend.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateNotificationSettingsRequest(
        @NotEmpty List<NotificationSettingUpdate> settings
) {
    public record NotificationSettingUpdate(
            @NotNull NotificationType type,
            boolean enabled
    ) {
    }
}
