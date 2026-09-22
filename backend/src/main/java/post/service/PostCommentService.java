package post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import post.dto.PostCommentCreateRequest;
import post.dto.PostCommentResponse;
import post.entity.Post;
import post.entity.PostComment;
import post.repository.PostCommentRepository;
import post.repository.PostRepository;
import user.entity.User;
import user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentService {
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<PostCommentResponse> getComments(Long postId){
        findActivePost(postId);

        return commentRepository.findAllByPostId(postId)
                .stream()
                .map(PostCommentResponse::from)
                .toList();
    }

    @Transactional
    public PostCommentResponse createComment(
            Long postId,
            Long userId,
            PostCommentCreateRequest request
    ) {
        Post post = findActivePost(postId);
        User writer = findUser(userId);

        PostComment parentComment = null;

        if(request.parentCommentId() != null){
            parentComment = findComment(request.parentCommentId());

            if(!parentComment.getPost().getPostId().equals(postId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "다른 게시글의 댓글에는 답글을 작성할 수 없습니다"
                );
            }

            if (!"Y".equals(parentComment.getUseYn())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "삭제된 댓글에는 답글을 작성할 수 없습니다"
                );
            }
        }

        PostComment comment = PostComment.create(
                post,
                writer,
                parentComment,
                request.context().trim()
        );

            PostComment savedComment =
                    commentRepository.save(comment);

            return PostCommentResponse.from(savedComment);

    }

    @Transactional
    public void deleteComment(
            Long postId,
            Long commentId,
            Long userId
    ){
        findActivePost(postId);
        PostComment comment = findComment(commentId);

        if (!comment.getPost().getPostId().equals(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 게시글의 댓글이 아닙니다."
            );
        }

        if (!comment.isWriter(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인이 작성한 댓글만 삭제할 수 있습니다."
            );
        }

        if (!"Y".equals(comment.getUseYn())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 삭제된 댓글입니다."
            );
        }

        // 답글 관계를 유지하기 위해 실제 삭제 대신 논리 삭제
        comment.delete();



    }



    private Post findActivePost(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"
                ));

        if(!"Y".equals(post.getUseYn())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "삭제되 게시물 입니다"
            );
        }
        return post;
    }

    private PostComment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"
                ));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"
                ));
    }
}
