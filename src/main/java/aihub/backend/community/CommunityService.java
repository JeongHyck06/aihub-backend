package aihub.backend.community;

import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import aihub.backend.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CommunityService {
    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityPostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<CommunityPostSummaryResponse> list(CommunityCategory category, Pageable pageable) {
        return postRepository.search(category, pageable).map(CommunityPostSummaryResponse::from);
    }

    @Transactional
    public CommunityPostDetailResponse get(Long id) {
        CommunityPost post = postRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public CommunityPostDetailResponse create(CurrentUser currentUser, CreateCommunityPostRequest request) {
        User author = loadUser(currentUser);
        CommunityPost post = new CommunityPost(author, request.category(), request.title(), request.body());
        return CommunityPostDetailResponse.from(postRepository.save(post));
    }

    @Transactional
    public CommunityPostDetailResponse update(Long id, CurrentUser currentUser, CreateCommunityPostRequest request) {
        CommunityPost post = loadEditable(id, currentUser);
        post.setCategory(request.category());
        post.setTitle(request.title());
        post.setBody(request.body());
        post.setUpdatedAt(Instant.now());
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public void delete(Long id, CurrentUser currentUser) {
        CommunityPost post = loadEditable(id, currentUser);
        post.setDeletedAt(Instant.now());
    }

    private CommunityPost loadEditable(Long id, CurrentUser currentUser) {
        CommunityPost post = postRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        boolean isAuthor = post.getAuthor().getId().equals(currentUser.id());
        boolean isAdmin = currentUser.role() == UserRole.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
        }
        return post;
    }

    private User loadUser(CurrentUser currentUser) {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
    }
}
