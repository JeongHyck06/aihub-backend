package aihub.backend.common.security;

import aihub.backend.user.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String createAccessToken(Long userId, String email, UserRole role) {
        return createToken(userId, email, role.name(), properties.accessTokenValiditySeconds(), "access");
    }

    public String createRefreshToken(Long userId, String email, UserRole role) {
        return createToken(userId, email, role.name(), properties.refreshTokenValiditySeconds(), "refresh");
    }

    public CurrentUser parseAccessToken(String token) {
        Map<String, Object> payload = parsePayload(token);
        if (!"access".equals(payload.get("type"))) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return toCurrentUser(payload);
    }

    public CurrentUser parseRefreshToken(String token) {
        Map<String, Object> payload = parsePayload(token);
        if (!"refresh".equals(payload.get("type"))) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return toCurrentUser(payload);
    }

    private CurrentUser toCurrentUser(Map<String, Object> payload) {
        Number exp = (Number) payload.get("exp");
        if (Instant.now().getEpochSecond() > exp.longValue()) {
            throw new IllegalArgumentException("Expired token");
        }
        Number sub = (Number) payload.get("sub");
        return new CurrentUser(
                sub.longValue(),
                (String) payload.get("email"),
                UserRole.valueOf((String) payload.get("role"))
        );
    }

    private String createToken(Long userId, String email, String role, long validitySeconds, String type) {
        try {
            String header = toBase64Json(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = toBase64Json(Map.of(
                    "sub", userId,
                    "email", email,
                    "role", role,
                    "type", type,
                    "jti", UUID.randomUUID().toString(),
                    "iat", Instant.now().getEpochSecond(),
                    "exp", Instant.now().plusSeconds(validitySeconds).getEpochSecond()
            ));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create JWT", exception);
        }
    }

    private Map<String, Object> parsePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                throw new IllegalArgumentException("Invalid signature");
            }
            String payloadJson = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            return objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JWT", exception);
        }
    }

    private String toBase64Json(Map<String, Object> value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
