package aihub.backend.config;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.catalog.ApiSupport;
import aihub.backend.catalog.Category;
import aihub.backend.catalog.CategoryRepository;
import aihub.backend.catalog.PricePolicy;
import aihub.backend.catalog.ServiceStatus;
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

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            CategoryRepository categoryRepository,
            AiServiceRepository aiServiceRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedUsers(userRepository, passwordEncoder);
            seedCategories(categoryRepository);
            seedServices(categoryRepository, aiServiceRepository);
        };
    }

    private void seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByEmail("admin@aihub.local")) {
            User admin = new User(
                    "admin@aihub.local",
                    passwordEncoder.encode("admin1234!"),
                    "AIHUB Admin",
                    UserRole.ADMIN,
                    true
            );
            userRepository.save(admin);
        }
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
