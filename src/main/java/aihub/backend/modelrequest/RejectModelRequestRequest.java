package aihub.backend.modelrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectModelRequestRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
