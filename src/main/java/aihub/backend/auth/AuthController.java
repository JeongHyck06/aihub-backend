package aihub.backend.auth;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @Operation(summary = "이메일 인증 코드 발송")
    @PostMapping("/email/send-code")
    public ApiResponse<SendCodeResponse> sendCode(@Valid @RequestBody EmailRequest request) {
        return ApiResponse.ok(authService.sendCode(request));
    }

    @Operation(summary = "이메일 인증")
    @PostMapping("/email/verify")
    public ApiResponse<AuthTokenResponse> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        return ApiResponse.ok(authService.verifyEmail(request));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @Operation(summary = "소셜 로그인 콜백 (Google/Kakao OIDC, GitHub OAuth)")
    @PostMapping("/oauth/{provider}/callback")
    public ApiResponse<AuthTokenResponse> oauthCallback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequest request
    ) {
        return ApiResponse.ok(authService.oauthLogin(provider, request));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CurrentUser currentUser) {
        authService.logout(currentUser);
        return ApiResponse.ok(null);
    }
}
