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
import post.dto.PostDeleteRequest;
import post.dto.PostDetailResponse;
import post.dto.PostListResponse;
import post.dto.PostSearchCondition;
import post.dto.PostUpdateRequest;
import post.entity.Post;
import post.repository.PostRepository;
import post.repository.PostSpecification;
import user.entity.User;
import user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
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
    public PostDetailResponse getPost(Long postId) {

        Post post = findActivePost(postId);

        post.increaseViewCount();

        return PostDetailResponse.from(post);
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
            PostDeleteRequest request
    ) {
        Post post = findActivePost(postId);

        validateWriterOrAdmin(
                post,
                request.userId(),
                request.admin()
        );

        post.delete(request.userId());
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