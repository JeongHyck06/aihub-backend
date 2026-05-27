package aihub.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aihub.cors")
public record CorsProperties(
        String allowedOrigins
) {
}
