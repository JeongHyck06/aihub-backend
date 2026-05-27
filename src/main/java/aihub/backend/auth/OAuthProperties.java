package aihub.backend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "aihub.oauth")
public record OAuthProperties(
        Map<String, OAuthProviderProperties> providers
) {
    public OAuthProviderProperties get(OAuthProvider provider) {
        OAuthProviderProperties properties = providers.get(provider.name().toLowerCase());
        if (properties == null) {
            throw new IllegalArgumentException("OAuth provider is not configured: " + provider);
        }
        return properties;
    }

    public record OAuthProviderProperties(
            String clientId,
            String clientSecret,
            String tokenUri,
            String issuerUri,
            String userInfoUri,
            String emailUri
    ) {
    }
}
