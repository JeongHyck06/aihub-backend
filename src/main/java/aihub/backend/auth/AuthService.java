package aihub.backend.auth;

import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import aihub.backend.common.security.JwtTokenProvider;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import aihub.backend.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {
    private static final long EMAIL_CODE_TTL_SECONDS = 300;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final OAuthClient oAuthClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            OAuthAccountRepository oAuthAccountRepository,
            OAuthClient oAuthClient,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.oAuthClient = oAuthClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                UserRole.USER,
                false
        );
        userRepository.save(user);
        EmailVerification verification = issueEmailCode(user);
        return new SignupResponse(user.getId(), user.getEmail(), true, verification.getCode());
    }

    @Transactional
    public SendCodeResponse sendCode(EmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가입된 이메일을 찾을 수 없습니다."));
        EmailVerification verification = issueEmailCode(user);
        return new SendCodeResponse(EMAIL_CODE_TTL_SECONDS, verification.getCode());
    }

    @Transactional
    public AuthTokenResponse verifyEmail(EmailVerifyRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가입된 이메일을 찾을 수 없습니다."));
        EmailVerification verification = emailVerificationRepository.findTopByUserOrderByIdDesc(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "인증 코드가 없습니다."));

        Instant now = Instant.now();
        if (verification.isExpired(now) || !verification.getCode().equals(request.code())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        verification.setVerifiedAt(now);
        user.setEmailVerified(true);
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이메일 인증이 필요합니다.");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshRequest request) {
        CurrentUser currentUser = jwtTokenProvider.parseRefreshToken(request.refreshToken());
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        if (!request.refreshToken().equals(user.getRefreshToken())) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다.");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(CurrentUser currentUser) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        user.setRefreshToken(null);
    }

    @Transactional
    public AuthTokenResponse oauthLogin(String providerPath, OAuthCallbackRequest request) {
        OAuthProvider provider = parseProvider(providerPath);
        OAuthProfile profile = oAuthClient.fetchProfile(provider, request);
        User user = oAuthAccountRepository.findByProviderAndProviderUserId(provider, profile.providerUserId())
                .map(OAuthAccount::getUser)
                .orElseGet(() -> linkOrCreateOAuthUser(profile));
        return issueTokens(user);
    }

    private EmailVerification issueEmailCode(User user) {
        String code = "%06d".formatted(ThreadLocalRandom.current().nextInt(0, 1_000_000));
        EmailVerification verification = new EmailVerification(user, code, Instant.now().plusSeconds(EMAIL_CODE_TTL_SECONDS));
        return emailVerificationRepository.save(verification);
    }

    private AuthTokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
        user.setRefreshToken(refreshToken);
        return new AuthTokenResponse(accessToken, refreshToken, AuthUserResponse.from(user));
    }

    private User linkOrCreateOAuthUser(OAuthProfile profile) {
        User user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> userRepository.save(new User(
                        profile.email(),
                        passwordEncoder.encode("OAUTH2:" + UUID.randomUUID()),
                        profile.name(),
                        UserRole.USER,
                        true
                )));
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
        }
        oAuthAccountRepository.save(new OAuthAccount(user, profile.provider(), profile.providerUserId(), profile.email()));
        return user;
    }

    private OAuthProvider parseProvider(String providerPath) {
        try {
            return OAuthProvider.fromPath(providerPath);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "지원하지 않는 소셜 로그인 제공자입니다.");
        }
    }
}
