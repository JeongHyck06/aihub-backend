package aihub.backend.common.api;

import java.util.Map;

public record ErrorResponse(
        ErrorBody error
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorBody(code, message, Map.of()));
    }

    public static ErrorResponse of(String code, String message, Map<String, String> fields) {
        return new ErrorResponse(new ErrorBody(code, message, fields));
    }

    public record ErrorBody(
            String code,
            String message,
            Map<String, String> fields
    ) {
    }
}
