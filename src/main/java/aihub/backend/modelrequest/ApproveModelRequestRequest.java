package aihub.backend.modelrequest;

import aihub.backend.catalog.ApiSupport;
import jakarta.validation.constraints.Size;

public record ApproveModelRequestRequest(
        @Size(max = 80) String slug,
        @Size(max = 100) String priceText,
        ApiSupport apiSupport
) {
}
