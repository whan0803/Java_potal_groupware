import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api, attachmentApi } from '../services/api.js';
import { useApp } from '../context/AppContext.jsx';

const viewedPostIds = new Set();

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
          <p className="mono">{post?.boardName ?? row[1]}</p>
          <h2>{post?.title ?? row[2]}</h2>
        </div>
        <span className="status-badge">
          <span />
          조회 {post?.viewCount ?? row[4]}
        </span>
      </div>
      <dl className="profile-grid">
        <div className="profile-field">
          <dt>작성자</dt>
          <dd>{post?.writerName ?? row[3]}</dd>
        </div>
        <div className="profile-field">
          <dt>등록일</dt>
          <dd>{String(post?.createdAt ?? row[5] ?? '').slice(0, 10)}</dd>
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
      {error ? <p className="form-error">{error}</p> : null}
      <div className="form-actions">
        <Link className="button secondary" to="/posts">목록</Link>
        {canEdit ? <Link className="button primary" to={`/posts/new?index=${index}`}>수정</Link> : null}
      </div>
    </section>
  );
}

function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export default PostDetail;
