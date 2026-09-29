import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api, attachmentApi } from '../services/api.js';
import { useApp } from '../context/AppContext.jsx';

const viewedPostIds = new Set();

const toVoteCount = (value) => {
  const count = Number(value);
  return Number.isFinite(count) ? Math.max(0, count) : 0;
};

function PostReactions({ postId, recommendCount, dislikeCount, myReaction, onChanged }) {
  const [counts, setCounts] = useState({ recommend: 0, dislike: 0 });
  const [selectedReaction, setSelectedReaction] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setCounts({
      recommend: toVoteCount(recommendCount),
      dislike: toVoteCount(dislikeCount),
    });
    setSelectedReaction(['recommend', 'dislike'].includes(myReaction) ? myReaction : null);
  }, [postId, recommendCount, dislikeCount, myReaction]);

  const handleReaction = async (reaction) => {
    if (!postId || submitting) return;

    const previousReaction = selectedReaction;
    const nextReaction = previousReaction === reaction ? null : reaction;
    setSubmitting(true);
    setError('');

    try {
      let detail;

      if (previousReaction === reaction) {
        detail = await api.patch(`/api/posts/${postId}/${previousReaction}/cancel`);
      } else {
        detail = await api.patch(`/api/posts/${postId}/${nextReaction}`);
      }

      setCounts({
        recommend: toVoteCount(detail?.recommendCount ?? counts.recommend),
        dislike: toVoteCount(detail?.dislikeCount ?? counts.dislike),
      });
      setSelectedReaction(detail?.myReaction ?? null);
      onChanged?.();
    } catch (reactionError) {
      setError(reactionError.message || '평가를 처리하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="post-reactions" aria-label="게시글 피드백">
      <div className="reaction-heading">
        <span>게시글 피드백</span>
        <strong>이 글이 도움이 되었나요?</strong>
      </div>
      <div className="reaction-options">
        <button
          aria-label={`좋아요 ${counts.recommend}개`}
          aria-pressed={selectedReaction === 'recommend'}
          className={`reaction-button recommend${selectedReaction === 'recommend' ? ' selected' : ''}`}
          disabled={submitting}
          onClick={() => handleReaction('recommend')}
          type="button"
        >
          <svg aria-hidden="true" viewBox="0 0 24 24">
            <path d="M7.8 10.2 11.6 3c.4-.8 1.5-.9 2.1-.3.4.4.5.9.4 1.4l-.7 4h4.5c1.5 0 2.6 1.4 2.2 2.8l-1.5 6.2c-.3 1.2-1.4 2.1-2.6 2.1H7.8v-9Z" />
            <path d="M3.5 9.8h4.3v9.7H3.5z" />
          </svg>
          <span>좋아요</span>
          <output>{counts.recommend}</output>
        </button>
        <button
          aria-label={`싫어요 ${counts.dislike}개`}
          aria-pressed={selectedReaction === 'dislike'}
          className={`reaction-button dislike${selectedReaction === 'dislike' ? ' selected' : ''}`}
          disabled={submitting}
          onClick={() => handleReaction('dislike')}
          type="button"
        >
          <svg aria-hidden="true" viewBox="0 0 24 24">
            <path d="m7.8 13.8 3.8 7.2c.4.8 1.5.9 2.1.3.4-.4.5-.9.4-1.4l-.7-4h4.5c1.5 0 2.6-1.4 2.2-2.8l-1.5-6.2c-.3-1.2-1.4-2.1-2.6-2.1H7.8v9Z" />
            <path d="M3.5 4.5h4.3v9.7H3.5z" />
          </svg>
          <span>싫어요</span>
          <output>{counts.dislike}</output>
        </button>
      </div>
      {error ? <p className="reaction-error">{error}</p> : null}
    </section>
  );
}

const normalizeComments = (comments = []) => comments.map((comment, index) => ({
  id: comment.commentId ?? comment.id ?? `comment-${index}`,
  content: comment.context ?? comment.content ?? comment.commentContent ?? '',
  createdAt: comment.createdAt ?? comment.createAt ?? '',
  deleted: Boolean(comment.deleted),
  parentCommentId: comment.parentCommentId ?? null,
  writerId: comment.writerId ?? comment.userId,
  writerName: comment.writerName ?? comment.userName ?? comment.author ?? '사용자',
}));

const formatCommentDate = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);

  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
};

function PostComments({ currentUser, postId }) {
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState('');
  const [sortOrder, setSortOrder] = useState('oldest');
  const [replyTarget, setReplyTarget] = useState(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;

    async function loadComments() {
      if (!postId) return;
      setLoading(true);
      setError('');
      try {
        const response = await api.get(`/api/posts/${postId}/comments`);
        if (mounted) setComments(normalizeComments(response));
      } catch (loadError) {
        if (mounted) setError(loadError.message || '댓글을 불러오지 못했습니다.');
      } finally {
        if (mounted) setLoading(false);
      }
    }

    setComments([]);
    setCommentText('');
    setSortOrder('oldest');
    setReplyTarget(null);
    loadComments();

    return () => {
      mounted = false;
    };
  }, [postId]);

  const orderedComments = [...comments].sort((left, right) => {
    const leftTime = new Date(left.createdAt).getTime() || 0;
    const rightTime = new Date(right.createdAt).getTime() || 0;
    return sortOrder === 'newest' ? rightTime - leftTime : leftTime - rightTime;
  });

  const submitComment = async () => {
    const content = commentText.trim();
    if (!content || !postId || submitting) return;

    setSubmitting(true);
    setError('');
    try {
      const savedComment = await api.post(`/api/posts/${postId}/comments`, {
        context: content,
        parentCommentId: replyTarget?.id ?? null,
      });
      setComments((current) => [...current, ...normalizeComments([savedComment])]);
      setCommentText('');
      setReplyTarget(null);
    } catch (submitError) {
      setError(submitError.message || '댓글을 등록하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const deleteComment = async (commentId) => {
    if (!postId || submitting) return;
    setSubmitting(true);
    setError('');
    try {
      await api.delete(`/api/posts/${postId}/comments/${commentId}`);
      setComments((current) => current.map((comment) => (
        comment.id === commentId
          ? { ...comment, content: '삭제된 댓글입니다.', deleted: true }
          : comment
      )));
      if (replyTarget?.id === commentId) setReplyTarget(null);
    } catch (deleteError) {
      setError(deleteError.message || '댓글을 삭제하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const isMyComment = (comment) => (
    comment.isLocal || (
      comment.writerId != null &&
      currentUser?.userId != null &&
      Number(comment.writerId) === Number(currentUser.userId)
    )
  );

  return (
    <section className="post-comments" aria-labelledby="comments-title">
      <div className="comments-toolbar">
        <div className="comments-heading">
          <div>
            <strong id="comments-title">댓글</strong>
            <span>{comments.length}</span>
          </div>
          <p>게시글에 대한 의견을 함께 나눠보세요.</p>
        </div>
        <div className="comment-sort" aria-label="댓글 정렬">
          <button
            className={sortOrder === 'oldest' ? 'active' : ''}
            onClick={() => setSortOrder('oldest')}
            type="button"
          >
            등록순
          </button>
          <button
            className={sortOrder === 'newest' ? 'active' : ''}
            onClick={() => setSortOrder('newest')}
            type="button"
          >
            최신순
          </button>
        </div>
      </div>

      <div className="comment-compose">
        <div className="comment-compose-head">
          <span className="comment-avatar" aria-hidden="true">
            {(currentUser?.name ?? '사').slice(0, 1)}
          </span>
          <div>
            <strong>{replyTarget ? '답글 작성' : '댓글 작성'}</strong>
            <span>{replyTarget ? `${replyTarget.writerName}님에게 답글을 남깁니다.` : (currentUser?.name ?? '사용자')}</span>
          </div>
          {replyTarget ? (
            <button onClick={() => setReplyTarget(null)} type="button">답글 취소</button>
          ) : null}
        </div>
        <div className="comment-input-wrap">
          <textarea
            aria-label="댓글 내용"
            maxLength={1000}
            onChange={(event) => setCommentText(event.target.value)}
            onKeyDown={(event) => {
              if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') submitComment();
            }}
            placeholder="내용을 입력하세요. 서로를 배려하는 댓글 문화를 함께 만들어주세요."
            value={commentText}
          />
          <div className="comment-compose-footer">
            <span>{commentText.length}/1000</span>
            <small>Ctrl(⌘)+Enter로 등록</small>
            <button
              className="comment-submit"
              disabled={!commentText.trim() || submitting}
              onClick={submitComment}
              type="button"
            >
              {submitting ? '등록 중' : (replyTarget ? '답글 등록' : '댓글 등록')}
            </button>
          </div>
        </div>
      </div>

      <div className="comment-list">
        {orderedComments.length ? orderedComments.map((comment) => (
          <article className={`comment-item${comment.parentCommentId ? ' reply' : ''}${comment.deleted ? ' deleted' : ''}`} key={comment.id}>
            <header className="comment-author">
              <span className="comment-avatar" aria-hidden="true">
                {(comment.writerName || '사').slice(0, 1)}
              </span>
              <div>
                <strong>{comment.writerName}</strong>
                <time dateTime={comment.createdAt}>{formatCommentDate(comment.createdAt)}</time>
              </div>
              {comment.parentCommentId ? <span className="reply-label">답글</span> : null}
            </header>
            <p>{comment.content}</p>
            <footer className="comment-actions">
              {!comment.deleted ? (
                <button
                  className="comment-reply"
                  onClick={() => setReplyTarget(comment)}
                  type="button"
                >
                  답글 작성
                </button>
              ) : null}
              {isMyComment(comment) && !comment.deleted ? (
                <button
                  aria-label={`${comment.writerName} 댓글 삭제`}
                  className="comment-delete"
                  disabled={submitting}
                  onClick={() => deleteComment(comment.id)}
                  type="button"
                >
                  삭제
                </button>
              ) : null}
            </footer>
          </article>
        )) : (
          <p className="comments-empty">
            {loading ? '댓글을 불러오는 중입니다.' : '아직 댓글이 없습니다. 첫 댓글을 남겨보세요.'}
          </p>
        )}
      </div>
      {error ? <p className="comment-error">{error}</p> : null}
    </section>
  );
}

function PostDetail() {
  const { user, lists, refreshBackendState } = useApp();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const index = Number.parseInt(searchParams.get('index') ?? '0', 10);
  const row = lists.posts.rows[index] ?? lists.posts.rows[0];
  const postId = row?._meta?.postId;
  const [post, setPost] = useState(row?._meta ?? null);
  const [attachments, setAttachments] = useState([]);
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);
  const canEdit = isAdminUser(user) || Number(post?.writerId ?? row?._meta?.writerId) === Number(user?.userId);

  const deletePost = async () => {
    if (!postId || deleting) return;
    if (!window.confirm('이 게시글을 삭제하시겠습니까?')) return;

    setDeleting(true);
    setError('');
    try {
      await api.patch(`/api/posts/${postId}/delete`, {
        userId: user?.userId,
        admin: isAdminUser(user),
      });
      await refreshBackendState();
      navigate('/posts', { replace: true });
    } catch (deleteError) {
      setError(deleteError.message || '게시글을 삭제하지 못했습니다.');
    } finally {
      setDeleting(false);
    }
  };

  useEffect(() => {
    let mounted = true;
    if (!postId) return undefined;

    async function loadPost() {
      try {
        const shouldIncreaseView = !viewedPostIds.has(postId);
        const detail = await api.get(`/api/posts/${postId}`, { increaseView: shouldIncreaseView });
        viewedPostIds.add(postId);
        const files = await attachmentApi.list('POST', postId).catch(() => []);
        if (!mounted) return;
        setPost(detail);
        setAttachments(files);
        refreshBackendState();
      } catch (loadError) {
        if (mounted) setError(loadError.message || '게시글을 불러오지 못했습니다.');
      }
    }

    loadPost();
    return () => {
      mounted = false;
    };
  }, [postId]);

  if (!row) {
    return (
      <section className="detail-card">
        <p className="form-error">게시글을 찾을 수 없습니다.</p>
        <Link className="button secondary" to="/posts">목록</Link>
      </section>
    );
  }

  return (
    <section className="detail-card">
      <div className="detail-head">
        <div>
          <p className="mono">{post?.boardName ?? row?._meta?.boardName}</p>
          <h2>{post?.title ?? row[1]}</h2>
        </div>
        <span className="status-badge">
          <span />
          조회 {post?.viewCount ?? row[4]}
        </span>
      </div>
      <dl className="profile-grid">
        <div className="profile-field">
          <dt>작성자</dt>
          <dd>{post?.writerName ?? row[2]}</dd>
        </div>
        <div className="profile-field">
          <dt>등록일</dt>
          <dd>{String(post?.createdAt ?? row[3] ?? '').slice(0, 10)}</dd>
        </div>
      </dl>
      <div className="content-preview">
        {post?.content || row?._meta?.content || '내용이 없습니다.'}
      </div>
      {attachments.length ? (
        <div className="attachment-list">
          {attachments.map((file) => (
            <a href={`/api/attachments/${file.attachmentId}/download`} key={file.attachmentId}>
              {file.originalName}
            </a>
          ))}
        </div>
      ) : null}
      <PostReactions
        dislikeCount={post?.dislikeCount ?? post?.unrecommendCount ?? row?._meta?.dislikeCount ?? row?._meta?.unrecommendCount}
        postId={postId}
        recommendCount={post?.recommendCount ?? row?._meta?.recommendCount ?? row?.[5]}
        myReaction={post?.myReaction}
        onChanged={refreshBackendState}
      />
      <PostComments
        currentUser={user}
        postId={postId}
      />
      {error ? <p className="form-error">{error}</p> : null}
      <div className="form-actions">
        <Link className="button secondary" to="/posts">목록</Link>
        {canEdit ? (
          <>
            <Link className="button primary" to={`/posts/new?index=${index}`}>수정</Link>
            <button
              className="button danger"
              disabled={deleting}
              onClick={deletePost}
              type="button"
            >
              {deleting ? '삭제 중' : '삭제'}
            </button>
          </>
        ) : null}
      </div>
    </section>
  );
}

function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export default PostDetail;
