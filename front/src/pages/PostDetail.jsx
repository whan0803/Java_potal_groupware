import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api, attachmentApi } from '../services/api.js';
import { useApp } from '../context/AppContext.jsx';

const viewedPostIds = new Set();

const toVoteCount = (value) => {
  const count = Number(value);
  return Number.isFinite(count) ? Math.max(0, count) : 0;
};

function PostReactions({ postId, recommendCount, dislikeCount, onChanged }) {
  const [counts, setCounts] = useState({ recommend: 0, dislike: 0 });
  const [selectedReaction, setSelectedReaction] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setCounts({
      recommend: toVoteCount(recommendCount),
      dislike: toVoteCount(dislikeCount),
    });
    setSelectedReaction(null);
  }, [postId, recommendCount, dislikeCount]);

  const handleReaction = async (reaction) => {
    if (!postId || submitting) return;

    const previousReaction = selectedReaction;
    const nextReaction = previousReaction === reaction ? null : reaction;
    setSubmitting(true);
    setError('');

    try {
      let detail;

      if (previousReaction) {
        detail = await api.patch(`/api/posts/${postId}/${previousReaction}/cancel`);
      }
      if (nextReaction) {
        detail = await api.patch(`/api/posts/${postId}/${nextReaction}`);
      }

      setCounts({
        recommend: toVoteCount(detail?.recommendCount ?? counts.recommend),
        dislike: toVoteCount(detail?.dislikeCount ?? counts.dislike),
      });
      setSelectedReaction(nextReaction);
      onChanged?.();
    } catch (reactionError) {
      setError(reactionError.message || '평가를 처리하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="post-reactions" aria-label="게시글 평가">
      <output className="reaction-count recommend-count" aria-label={`추천 ${counts.recommend}개`}>
        {counts.recommend}
      </output>
      <button
        aria-label="이 게시글 추천"
        aria-pressed={selectedReaction === 'recommend'}
        className={`reaction-button recommend${selectedReaction === 'recommend' ? ' selected' : ''}`}
        disabled={submitting}
        onClick={() => handleReaction('recommend')}
        type="button"
      >
        <svg aria-hidden="true" viewBox="0 0 48 48">
          <path d="m24 5.2 5.7 11.6 12.8 1.9-9.3 9 2.2 12.7L24 34.5l-11.4 5.9 2.2-12.7-9.3-9 12.8-1.9L24 5.2Z" />
        </svg>
        <span>개념</span>
      </button>
      <button
        aria-label="이 게시글 비추천"
        aria-pressed={selectedReaction === 'dislike'}
        className={`reaction-button dislike${selectedReaction === 'dislike' ? ' selected' : ''}`}
        disabled={submitting}
        onClick={() => handleReaction('dislike')}
        type="button"
      >
        <svg aria-hidden="true" viewBox="0 0 48 48">
          <path d="M19 5h10v19h9L24 43 10 24h9V5Z" />
        </svg>
        <span>비추</span>
      </button>
      <output className="reaction-count dislike-count" aria-label={`비추천 ${counts.dislike}개`}>
        {counts.dislike}
      </output>
      {error ? <p className="reaction-error">{error}</p> : null}
    </div>
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
        <strong id="comments-title">
          전체 댓글 <span>{comments.length}</span>개
        </strong>
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

      <div className="comment-list">
        {orderedComments.length ? orderedComments.map((comment) => (
          <article className={`comment-item${comment.parentCommentId ? ' reply' : ''}`} key={comment.id}>
            <div className="comment-author">
              <span className="comment-avatar" aria-hidden="true">
                {(comment.writerName || '사').slice(0, 1)}
              </span>
              <strong>{comment.writerName}</strong>
            </div>
            <p>{comment.content}</p>
            <time dateTime={comment.createdAt}>{formatCommentDate(comment.createdAt)}</time>
            <div className="comment-actions">
              {!comment.deleted ? (
                <button
                  className="comment-reply"
                  onClick={() => setReplyTarget(comment)}
                  type="button"
                >
                  답글
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
                  ×
                </button>
              ) : null}
            </div>
          </article>
        )) : (
          <p className="comments-empty">
            {loading ? '댓글을 불러오는 중입니다.' : '아직 댓글이 없습니다. 첫 댓글을 남겨보세요.'}
          </p>
        )}
      </div>

      <div className="comment-compose">
        <strong>
          {replyTarget ? `${replyTarget.writerName}에게 답글` : (currentUser?.name ?? '사용자')}
          {replyTarget ? (
            <button aria-label="답글 작성 취소" onClick={() => setReplyTarget(null)} type="button">×</button>
          ) : null}
        </strong>
        <div className="comment-input-wrap">
          <textarea
            aria-label="댓글 내용"
            maxLength={1000}
            onChange={(event) => setCommentText(event.target.value)}
            onKeyDown={(event) => {
              if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') submitComment();
            }}
            placeholder="댓글을 입력하세요. Ctrl(⌘)+Enter로 등록할 수 있습니다."
            value={commentText}
          />
          <span>{commentText.length}/1000</span>
        </div>
        <button
          className="comment-submit"
          disabled={!commentText.trim() || submitting}
          onClick={submitComment}
          type="button"
        >
          {submitting ? '처리 중' : '등록'}
        </button>
      </div>
      {error ? <p className="comment-error">{error}</p> : null}
    </section>
  );
}

function PostDetail() {
  const { user, lists, refreshBackendState } = useApp();
  const [searchParams] = useSearchParams();
  const index = Number.parseInt(searchParams.get('index') ?? '0', 10);
  const row = lists.posts.rows[index] ?? lists.posts.rows[0];
  const postId = row?._meta?.postId;
  const [post, setPost] = useState(row?._meta ?? null);
  const [attachments, setAttachments] = useState([]);
  const [error, setError] = useState('');
  const canEdit = isAdminUser(user) || Number(post?.writerId ?? row?._meta?.writerId) === Number(user?.userId);

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
            <button className="button danger" type="button">삭제</button>
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
