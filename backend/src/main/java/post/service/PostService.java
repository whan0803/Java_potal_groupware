package post.service;

import boards.entity.Board;
import boards.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import post.dto.PostCreateRequest;
import post.dto.PostDetailResponse;
import post.dto.PostListResponse;
import post.dto.PostSearchCondition;
import post.dto.PostUpdateRequest;
import post.entity.Post;
import post.entity.PostReaction;
import post.entity.PostReactionType;
import post.repository.PostReactionRepository;
import post.repository.PostRepository;
import post.repository.PostSpecification;
import user.entity.User;
import user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostReactionRepository postReactionRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    // 게시글 목록 조회, 검색, 페이징
    public Page<PostListResponse> getPosts(
            PostSearchCondition condition,
            Pageable pageable
    ) {
        Specification<Post> specification =
                PostSpecification.useYnEquals("Y")
                        .and(
                                PostSpecification.boardIdEquals(
                                        condition.boardId()
                                )
                        )
                        .and(
                                PostSpecification.search(
                                        condition.searchType(),
                                        condition.keyword()
                                )
                        );

        Page<Post> posts =
                postRepository.findAll(specification, pageable);

        return posts.map(PostListResponse::from);
    }

    // 게시글 상세 조회 및 조회수 증가
    @Transactional
    public PostDetailResponse getPost(
            Long postId,
            boolean increaseView,
            Long userId
    ) {

        Post post = findActivePost(postId);

        if (increaseView) {
            post.increaseViewCount();
        }

        return response(post, userId);
    }

    // 게시글 등록
    @Transactional
    public Long createPost(PostCreateRequest request) {

        Board board = findActiveBoard(request.boardId());
        User writer = findUser(request.writerId());

        Post post = Post.create(
                board,
                request.title(),
                request.content(),
                writer
        );

        return postRepository.save(post).getPostId();
    }

    // 게시글 수정
    @Transactional
    public void updatePost(
            Long postId,
            PostUpdateRequest request
    ) {
        Post post = findActivePost(postId);

        validateWriterOrAdmin(
                post,
                request.userId(),
                request.admin()
        );

        post.update(
                request.title(),
                request.content(),
                request.userId()
        );
    }

    // 게시글 논리 삭제
    @Transactional
    public void deletePost(
            Long postId,
            Long userId,
            boolean admin
    ) {
        Post post = findActivePost(postId);

        validateWriterOrAdmin(
                post,
                userId,
                admin
        );

        post.delete(userId);
    }

    // 사용자별 좋아요 등록 또는 싫어요에서 변경
    @Transactional
    public PostDetailResponse recommendPost(Long postId, Long userId) {
        return react(postId, userId, PostReactionType.RECOMMEND);
    }

    // 사용자별 좋아요 취소
    @Transactional
    public PostDetailResponse cancelRecommendPost(Long postId, Long userId){
        return cancelReaction(postId, userId, PostReactionType.RECOMMEND);
    }

    // 사용자별 싫어요 등록 또는 좋아요에서 변경
    @Transactional
    public PostDetailResponse dislikePost(Long postId, Long userId) {
        return react(postId, userId, PostReactionType.DISLIKE);
    }

    // 사용자별 싫어요 취소
    @Transactional
    public PostDetailResponse cancelDislikePost(Long postId, Long userId) {
        return cancelReaction(postId, userId, PostReactionType.DISLIKE);
    }

    private PostDetailResponse react(
            Long postId,
            Long userId,
            PostReactionType nextType
    ) {
        Post post = findActivePostForUpdate(postId);
        PostReaction reaction = postReactionRepository
                .findByPostPostIdAndUserUserId(postId, userId)
                .orElse(null);

        if (reaction == null) {
            User user = findUser(userId);
            postReactionRepository.save(PostReaction.create(post, user, nextType));
            increaseCount(post, nextType);
        } else if (reaction.getReactionType() != nextType) {
            decreaseCount(post, reaction.getReactionType());
            increaseCount(post, nextType);
            reaction.changeType(nextType);
        }

        return PostDetailResponse.from(post, nextType.getApiValue());
    }

    private PostDetailResponse cancelReaction(
            Long postId,
            Long userId,
            PostReactionType cancelType
    ) {
        Post post = findActivePostForUpdate(postId);
        PostReaction reaction = postReactionRepository
                .findByPostPostIdAndUserUserId(postId, userId)
                .orElse(null);

        if (reaction != null && reaction.getReactionType() == cancelType) {
            postReactionRepository.delete(reaction);
            decreaseCount(post, cancelType);
            return PostDetailResponse.from(post, null);
        }

        String currentReaction = reaction == null
                ? null
                : reaction.getReactionType().getApiValue();
        return PostDetailResponse.from(post, currentReaction);
    }

    private void increaseCount(Post post, PostReactionType reactionType) {
        if (reactionType == PostReactionType.RECOMMEND) {
            post.increaseRecommendCount();
        } else {
            post.increaseDislikeCount();
        }
    }

    private void decreaseCount(Post post, PostReactionType reactionType) {
        if (reactionType == PostReactionType.RECOMMEND) {
            post.decreaseRecommendCount();
        } else {
            post.decreaseDislikeCount();
        }
    }

    private PostDetailResponse response(Post post, Long userId) {
        String myReaction = postReactionRepository
                .findByPostPostIdAndUserUserId(post.getPostId(), userId)
                .map(PostReaction::getReactionType)
                .map(PostReactionType::getApiValue)
                .orElse(null);
        return PostDetailResponse.from(post, myReaction);
    }


    // 사용 중인 게시글 조회
    private Post findActivePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다. postId: "
                                        + postId
                        )
                );

        if (!"Y".equals(post.getUseYn())) {
            throw new IllegalStateException(
                    "삭제되었거나 사용 중지된 게시글입니다."
            );
        }

        return post;
    }

    private Post findActivePostForUpdate(Long postId) {
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다. postId: " + postId
                        )
                );

        if (!"Y".equals(post.getUseYn())) {
            throw new IllegalStateException(
                    "삭제되었거나 사용 중지된 게시글입니다."
            );
        }

        return post;
    }

    // 사용 중인 게시판 조회
    private Board findActiveBoard(Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시판을 찾을 수 없습니다. boardId: "
                                        + boardId
                        )
                );

        if (!"Y".equals(board.getUseYn())) {
            throw new IllegalStateException(
                    "사용 중지된 게시판에는 게시글을 등록할 수 없습니다."
            );
        }

        return board;
    }

    // 사용자 조회
    private User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다. userId: "
                                        + userId
                        )
                );
    }

    // 작성자 또는 관리자 여부 확인
    private void validateWriterOrAdmin(
            Post post,
            Long userId,
            boolean admin
    ) {
        if (!post.isWriter(userId) && !admin) {
            throw new IllegalStateException(
                    "게시글을 수정하거나 삭제할 권한이 없습니다."
            );
        }
    }
}
