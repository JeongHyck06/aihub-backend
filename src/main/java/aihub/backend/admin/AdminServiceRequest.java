package aihub.backend.admin;

import aihub.backend.catalog.ApiSupport;
import aihub.backend.catalog.PricePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminServiceRequest(
        @NotBlank @Size(max = 80) String slug,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String provider,
        @NotBlank @Size(max = 50) String categorySlug,
        @NotBlank @Size(max = 100) String priceText,
        @NotNull PricePolicy pricePolicy,
        @NotBlank String description,
        @Size(max = 80) String tagline,
        @NotBlank @Size(max = 300) String url,
        @Size(max = 300) String apiDocUrl,
        @NotNull ApiSupport apiSupport,
        @NotEmpty List<@NotBlank @Size(max = 120) String> features,
        @NotEmpty List<@NotBlank @Size(max = 50) String> tags
) {
}
