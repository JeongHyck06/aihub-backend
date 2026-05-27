package aihub.backend.auth;

import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Component
public class OAuthClient {
    private final OAuthProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public OAuthClient(OAuthProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public OAuthProfile fetchProfile(OAuthProvider provider, OAuthCallbackRequest request) {
        OAuthProperties.OAuthProviderProperties providerProperties = properties.get(provider);
        OAuthTokenResponse token = exchangeToken(providerProperties, request);
        return switch (provider) {
            case GOOGLE, KAKAO -> fetchOidcProfile(provider, providerProperties, token);
            case GITHUB -> fetchGithubProfile(providerProperties, token);
        };
    }

    private OAuthTokenResponse exchangeToken(
            OAuthProperties.OAuthProviderProperties providerProperties,
            OAuthCallbackRequest request
    ) {
        try {
            return restClient.post()
                    .uri(providerProperties.tokenUri())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody(providerProperties, request))
                    .retrieve()
                    .body(OAuthTokenResponse.class);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "소셜 로그인 토큰 교환에 실패했습니다.");
        }
    }

    private String formBody(
            OAuthProperties.OAuthProviderProperties providerProperties,
            OAuthCallbackRequest request
    ) {
        return "grant_type=authorization_code"
                + "&code=" + encode(request.code())
                + "&redirect_uri=" + encode(request.redirectUri())
                + "&client_id=" + encode(providerProperties.clientId())
                + "&client_secret=" + encode(providerProperties.clientSecret());
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private OAuthProfile fetchOidcProfile(
            OAuthProvider provider,
            OAuthProperties.OAuthProviderProperties providerProperties,
            OAuthTokenResponse token
    ) {
        if (token == null || token.idToken() == null || token.idToken().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "OIDC ID 토큰이 없습니다.");
        }

        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(providerProperties.issuerUri());
        Jwt jwt = decoder.decode(token.idToken());
        if (!jwt.getAudience().contains(providerProperties.clientId())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "OIDC audience가 올바르지 않습니다.");
        }

        String subject = jwt.getSubject();
        String email = claimAsString(jwt, "email");
        String name = claimAsString(jwt, "name");
        String picture = claimAsString(jwt, "picture");

        if (email == null || email.isBlank() || name == null || name.isBlank() || picture == null || picture.isBlank()) {
            JsonNode userInfo = fetchJson(providerProperties.userInfoUri(), token.accessToken());
            if (email == null || email.isBlank()) {
                email = readText(userInfo, "email");
            }
            if (name == null || name.isBlank()) {
                name = readText(userInfo, "name");
            }
            if (picture == null || picture.isBlank()) {
                picture = readText(userInfo, "picture");
            }
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "소셜 계정 이메일을 확인할 수 없습니다.");
        }
        return new OAuthProfile(
                provider,
                subject,
                email,
                name == null || name.isBlank() ? email : name,
                picture
        );
    }

    private OAuthProfile fetchGithubProfile(
            OAuthProperties.OAuthProviderProperties providerProperties,
            OAuthTokenResponse token
    ) {
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "GitHub access token이 없습니다.");
        }
        JsonNode user = fetchJson(providerProperties.userInfoUri(), token.accessToken());
        String id = readText(user, "id");
        String name = readText(user, "name");
        if (name == null || name.isBlank()) {
            name = readText(user, "login");
        }

        String email = readText(user, "email");
        if (email == null || email.isBlank()) {
            email = fetchPrimaryGithubEmail(providerProperties.emailUri(), token.accessToken());
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "GitHub 계정 이메일을 확인할 수 없습니다.");
        }
        return new OAuthProfile(OAuthProvider.GITHUB, id, email, name == null || name.isBlank() ? email : name, null);
    }

    private JsonNode fetchJson(String uri, String accessToken) {
        try {
            String body = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "소셜 사용자 정보 조회에 실패했습니다.");
        }
    }

    private String fetchPrimaryGithubEmail(String uri, String accessToken) {
        JsonNode emails = fetchJson(uri, accessToken);
        if (!emails.isArray()) {
            return null;
        }
        Iterator<JsonNode> iterator = emails.elements();
        while (iterator.hasNext()) {
            JsonNode email = iterator.next();
            if (email.path("primary").asBoolean(false) && email.path("verified").asBoolean(false)) {
                return readText(email, "email");
            }
        }
        return null;
    }

    private String claimAsString(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        return value == null ? null : value.toString();
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
