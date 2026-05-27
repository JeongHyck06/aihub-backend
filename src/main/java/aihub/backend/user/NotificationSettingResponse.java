package aihub.backend.user;

public record NotificationSettingResponse(
        String type,
        String label,
        boolean enabled
) {
    public static NotificationSettingResponse of(NotificationType type, boolean enabled) {
        return new NotificationSettingResponse(type.name(), type.label(), enabled);
    }
}
