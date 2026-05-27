package aihub.backend.modelrequest;

import aihub.backend.catalog.PricePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateModelRequestRequest(
        @NotBlank @Size(min = 1, max = 50) String serviceName,
        @NotBlank @Size(max = 50) String categorySlug,
        @NotBlank @Size(max = 300) String url,
        @NotNull PricePolicy pricePolicy,
        @NotBlank @Size(min = 50) String description,
        @NotEmpty @Size(max = 6) List<@NotBlank @Size(max = 120) String> features,
        @Size(max = 300) String apiDocUrl
) {
}
