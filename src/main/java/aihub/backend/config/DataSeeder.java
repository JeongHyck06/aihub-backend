package aihub.backend.config;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.catalog.ApiSupport;
import aihub.backend.catalog.Category;
import aihub.backend.catalog.CategoryRepository;
import aihub.backend.catalog.PricePolicy;
import aihub.backend.catalog.ServiceStatus;
import aihub.backend.catalog.review.Review;
import aihub.backend.catalog.review.ReviewRepository;
import aihub.backend.community.CommunityCategory;
import aihub.backend.community.CommunityPost;
import aihub.backend.community.CommunityPostRepository;
import aihub.backend.modelrequest.ModelRequest;
import aihub.backend.modelrequest.ModelRequestRepository;
import aihub.backend.modelrequest.ModelRequestStatus;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import aihub.backend.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            CategoryRepository categoryRepository,
            AiServiceRepository aiServiceRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            CommunityPostRepository communityPostRepository,
            ModelRequestRepository modelRequestRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            User admin = seedAdmin(userRepository, passwordEncoder);
            User demoUser = seedDemoUser(userRepository, passwordEncoder);
            seedCategories(categoryRepository);
            seedServices(categoryRepository, aiServiceRepository);
            seedReviews(reviewRepository, aiServiceRepository, demoUser, admin);
            seedCommunityPosts(communityPostRepository, demoUser, admin);
            seedModelRequests(modelRequestRepository, categoryRepository, demoUser);
        };
    }

    private User seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.findByEmail("admin@aihub.local").orElseGet(() -> {
            User user = new User(
                    "admin@aihub.local",
                    passwordEncoder.encode("admin1234!"),
                    "AIHUB Admin",
                    UserRole.ADMIN,
                    true
            );
            user.setBio("AIHUB 운영팀");
            return userRepository.save(user);
        });
    }

    private User seedDemoUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.findByEmail("demo@aihub.local").orElseGet(() -> {
            User user = new User(
                    "demo@aihub.local",
                    passwordEncoder.encode("demo1234!"),
                    "Demo",
                    UserRole.USER,
                    true
            );
            user.setDisplayName("나무를 탐색하고 있는 쿼카");
            user.setBio("나뭇잎 맛있다");
            return userRepository.save(user);
        });
    }

    private void seedCategories(CategoryRepository categoryRepository) {
        saveCategory(categoryRepository, "coding", "코딩 자동화", "Cursor, Copilot, Claude Code", 1);
        saveCategory(categoryRepository, "writing", "글쓰기 · 문서", "ChatGPT, Claude, Notion AI", 2);
        saveCategory(categoryRepository, "image", "이미지 생성", "Midjourney, DALL-E, Stable Diffusion", 3);
        saveCategory(categoryRepository, "music", "음악 생성", "Suno, Udio, ElevenLabs", 4);
        saveCategory(categoryRepository, "video", "영상 생성", "Sora, Runway, Pika", 5);
        saveCategory(categoryRepository, "qa", "검색 · Q&A", "Perplexity, You.com, Phind", 6);
    }

    private void seedServices(CategoryRepository categoryRepository, AiServiceRepository aiServiceRepository) {
        if (aiServiceRepository.existsBySlug("chatgpt")) {
            return;
        }
        saveService(
                aiServiceRepository,
                categoryRepository.findBySlug("writing").orElseThrow(),
                "chatgpt",
                "ChatGPT",
                "OpenAI",
                "무료/Plus $20",
                PricePolicy.FREEMIUM,
                """
                        ChatGPT는 OpenAI가 개발한 대형 언어 모델 기반의 AI 어시스턴트입니다.
                        코딩 자동완성, 디버깅, 문서 작성, 요약 등 다양한 작업에 활용할 수 있습니다.
                        """,
                "범용 대화부터 코딩 보조까지 폭넓게 활용 가능",
                "https://chat.openai.com",
                ApiSupport.YES,
                BigDecimal.valueOf(4.8),
                1243,
                List.of("코드 자동완성 및 디버깅 지원", "다양한 플러그인 및 API 연동 지원", "멀티모달 작업 지원"),
                List.of("코딩", "글쓰기", "GPT-4o", "API")
        );
        saveService(
                aiServiceRepository,
                categoryRepository.findBySlug("coding").orElseThrow(),
                "cursor",
                "Cursor",
                "Anysphere",
                "무료/Pro $20",
                PricePolicy.FREEMIUM,
                """
                        Cursor는 코드베이스 맥락을 이해하고 개발 작업을 도와주는 AI 코드 에디터입니다.
                        파일 탐색, 코드 수정, 리팩터링, 테스트 작성 같은 실제 구현 흐름에 강점이 있습니다.
                        """,
                "코드베이스를 이해하고 수정과 리팩터링을 돕는 AI 에디터",
                "https://cursor.com",
                ApiSupport.LIMITED,
                BigDecimal.valueOf(4.9),
                734,
                List.of("프로젝트 맥락 기반 질문과 수정", "에디터 안에서 직접 적용 가능한 코드 제안", "대규모 리팩터링 지원"),
                List.of("코딩", "IDE", "자동화")
        );
        saveService(
                aiServiceRepository,
                categoryRepository.findBySlug("writing").orElseThrow(),
                "claude",
                "Claude",
                "Anthropic",
                "무료/Pro $20",
                PricePolicy.FREEMIUM,
                "Claude는 긴 문서 분석, 글쓰기, 자연스러운 요약 작업에 강한 AI 어시스턴트입니다.",
                "긴 문서 분석과 자연스러운 글쓰기에 강한 AI 어시스턴트",
                "https://claude.ai",
                ApiSupport.YES,
                BigDecimal.valueOf(4.7),
                892,
                List.of("긴 컨텍스트 처리", "자연스러운 문장 작성", "분석 작업 지원"),
                List.of("글쓰기", "문서", "요약", "분석")
        );
        saveService(
                aiServiceRepository,
                categoryRepository.findBySlug("image").orElseThrow(),
                "midjourney",
                "Midjourney",
                "Midjourney",
                "월 $10부터",
                PricePolicy.PAID,
                "Midjourney는 고품질 비주얼 콘셉트와 스타일 탐색에 특화된 이미지 생성 도구입니다.",
                "고품질 비주얼 콘셉트와 스타일 탐색에 특화된 이미지 생성 도구",
                "https://midjourney.com",
                ApiSupport.NO,
                BigDecimal.valueOf(4.6),
                612,
                List.of("이미지 품질", "스타일 다양성", "콘셉트 시안 제작"),
                List.of("이미지", "디자인", "비주얼")
        );
        saveService(
                aiServiceRepository,
                categoryRepository.findBySlug("qa").orElseThrow(),
                "perplexity",
                "Perplexity",
                "Perplexity AI",
                "무료/Pro $20",
                PricePolicy.FREEMIUM,
                "Perplexity는 출처 기반 답변과 리서치 흐름에 특화된 AI 검색 서비스입니다.",
                "출처와 함께 정리해 주는 AI 검색 어시스턴트",
                "https://perplexity.ai",
                ApiSupport.YES,
                BigDecimal.valueOf(4.5),
                421,
                List.of("출처 인용", "주제별 검색 정리", "팔로업 질문"),
                List.of("검색", "리서치", "Q&A")
        );
    }

    private void seedReviews(
            ReviewRepository reviewRepository,
            AiServiceRepository aiServiceRepository,
            User demoUser,
            User admin
    ) {
        if (reviewRepository.count() > 0) {
            return;
        }
        Optional<AiService> chatgpt = aiServiceRepository.findBySlug("chatgpt");
        Optional<AiService> cursor = aiServiceRepository.findBySlug("cursor");
        chatgpt.ifPresent(service -> {
            reviewRepository.save(new Review(service, demoUser, 5,
                    "코딩 사이드에서 정말 유용하게 쓰고 있습니다. GPT-4o의 코드 생성 능력이 특히 뛰어납니다."));
            reviewRepository.save(new Review(service, admin, 4,
                    "플러그인과 API 연동이 정말 편리합니다."));
        });
        cursor.ifPresent(service -> reviewRepository.save(new Review(service, demoUser, 5,
                "프로젝트 맥락을 이해하고 여러 파일을 함께 수정해줘서 실제 개발 속도가 많이 빨라졌습니다.")));
    }

    private void seedCommunityPosts(
            CommunityPostRepository communityPostRepository,
            User demoUser,
            User admin
    ) {
        if (communityPostRepository.count() > 0) {
            return;
        }
        CommunityPost post1 = new CommunityPost(
                demoUser,
                CommunityCategory.QUESTION,
                "Cursor vs Copilot 코딩 보조 AI 어떤 게 더 낫나요?",
                "프리랜서로 React 프로젝트 진행 중인데, 코딩 AI를 마음에 드는 AI로 교체하려고 합니다. 두 서비스 사용해보신 분 의견 고마워요!"
        );
        post1.setViewCount(342);
        post1.setCommentCount(28);
        post1.setLikeCount(47);
        communityPostRepository.save(post1);

        CommunityPost post2 = new CommunityPost(
                admin,
                CommunityCategory.FREE,
                "2026 상반기 AI 도구 사용 후기 공유",
                "스타트업에서 마케터로 일하고 있습니다. Notion AI + ChatGPT 조합이 제일 잘 맞는 것 같아서 정리해보려고 합니다."
        );
        post2.setViewCount(1204);
        post2.setCommentCount(83);
        post2.setLikeCount(231);
        communityPostRepository.save(post2);

        CommunityPost post3 = new CommunityPost(
                demoUser,
                CommunityCategory.QUESTION,
                "Midjourney 없이 무료로 상업가능한 이미지 생성 AI 추천해주세요",
                "디자이너 입장에서 무료로 상업 사용이 가능한 이미지 생성 AI가 궁금합니다. 추천 부탁드립니다!"
        );
        post3.setViewCount(892);
        post3.setCommentCount(54);
        post3.setLikeCount(128);
        communityPostRepository.save(post3);
    }

    private void seedModelRequests(
            ModelRequestRepository modelRequestRepository,
            CategoryRepository categoryRepository,
            User demoUser
    ) {
        if (modelRequestRepository.count() > 0) {
            return;
        }
        Category video = categoryRepository.findBySlug("video").orElseThrow();
        ModelRequest sora = new ModelRequest();
        sora.setSubmitter(demoUser);
        sora.setServiceName("Sora");
        sora.setCategory(video);
        sora.setUrl("https://sora.com");
        sora.setPricePolicy(PricePolicy.PAID);
        sora.setDescription("OpenAI Sora는 텍스트 프롬프트만으로 고품질 영상을 생성하는 영상 생성 AI 서비스입니다. 영상의 길이와 장면 전환을 자연스럽게 처리합니다.");
        sora.getFeatures().addAll(List.of(
                "텍스트 프롬프트 기반 영상 생성",
                "고품질 장면 전환 지원",
                "다양한 스타일 옵션"
        ));
        sora.setStatus(ModelRequestStatus.PENDING);
        modelRequestRepository.save(sora);

        Category music = categoryRepository.findBySlug("music").orElseThrow();
        ModelRequest udio = new ModelRequest();
        udio.setSubmitter(demoUser);
        udio.setServiceName("Udio");
        udio.setCategory(music);
        udio.setUrl("https://udio.com");
        udio.setPricePolicy(PricePolicy.FREEMIUM);
        udio.setDescription("Udio는 가사와 장르를 입력하면 완성도 높은 음악을 생성하는 AI 음악 생성 서비스로, 다양한 장르를 지원합니다.");
        udio.getFeatures().addAll(List.of(
                "가사+장르 기반 음악 생성",
                "다양한 스타일 지원",
                "공유 가능한 결과물"
        ));
        udio.setStatus(ModelRequestStatus.PENDING);
        modelRequestRepository.save(udio);
    }

    private void saveCategory(CategoryRepository repository, String slug, String name, String description, int sortOrder) {
        if (!repository.existsBySlug(slug)) {
            repository.save(new Category(slug, name, description, sortOrder));
        }
    }

    private void saveService(
            AiServiceRepository repository,
            Category category,
            String slug,
            String name,
            String provider,
            String priceText,
            PricePolicy pricePolicy,
            String description,
            String tagline,
            String url,
            ApiSupport apiSupport,
            BigDecimal rating,
            int reviewCount,
            List<String> features,
            List<String> tags
    ) {
        AiService service = new AiService();
        service.setSlug(slug);
        service.setName(name);
        service.setProvider(provider);
        service.setCategory(category);
        service.setPriceText(priceText);
        service.setPricePolicy(pricePolicy);
        service.setDescription(description.strip());
        service.setTagline(tagline);
        service.setUrl(url);
        service.setApiSupport(apiSupport);
        service.setRatingAvg(rating);
        service.setReviewCount(reviewCount);
        service.setLastUpdatedAt(LocalDate.now());
        service.setStatus(ServiceStatus.PUBLISHED);
        service.getFeatures().addAll(features);
        service.getTags().addAll(tags);
        repository.save(service);
    }
}
