package aihub.backend.auth;

public enum OAuthProvider {
    GOOGLE,
    KAKAO,
    GITHUB;

    public static OAuthProvider fromPath(String provider) {
        return OAuthProvider.valueOf(provider.toUpperCase());
    }
}
