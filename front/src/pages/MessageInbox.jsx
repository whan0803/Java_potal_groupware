import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';

function MessageInbox({ mode = 'inbox' }) {
  const { user, messages, sentMessages, refreshBackendState } = useApp();
  const isSent = mode === 'sent';
  const isEmpty = mode === 'empty';
  const visibleMessages = isSent ? sentMessages : messages;
  const [selectedIndex, setSelectedIndex] = useState(null);
  const selectedMessage = useMemo(() => {
    if (selectedIndex === null) return null;
    return visibleMessages[selectedIndex] ?? null;
  }, [selectedIndex, visibleMessages]);
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');

  const selectMessage = async (index) => {
    setSelectedIndex(index);
    setError('');
    const row = visibleMessages[index];
    const messageId = row?._meta?.messageId;
    if (!messageId) {
      setDetail(null);
      return;
    }
    try {
      const response = await api.get(`/api/messages/${messageId}`, { userId: user.userId });
      setDetail(response);
      if (!isSent) refreshBackendState();
    } catch (loadError) {
      setError(loadError.message || '쪽지를 불러오지 못했습니다.');
    }
  };

  const deleteMessage = async () => {
    if (!selectedMessage?._meta?.messageId) return;
    try {
      await api.delete(`/api/messages/${selectedMessage._meta.messageId}/${isSent ? 'sent' : 'received'}?userId=${user.userId}`);
    } catch (deleteError) {
      setError(deleteError.message || '쪽지 삭제에 실패했습니다.');
      return;
    }
    setSelectedIndex(null);
    setDetail(null);
    await refreshBackendState();
  };

  const replyMessage = async () => {
    if (!selectedMessage?._meta?.messageId) return;
    const content = window.prompt('답장 내용을 입력하세요.');
    if (!content?.trim()) return;
    const title = detail?.title?.startsWith('RE:') ? detail.title : `RE: ${detail?.title ?? selectedMessage[2]}`;
    await api.post(`/api/messages/${selectedMessage._meta.messageId}/reply?userId=${user.userId}`, { title, content });
    window.alert('답장을 보냈습니다.');
    await refreshBackendState();
  };

  return (
    <section className="message-layout">
      <div className="message-list content-card">
        <div className="tabs">
          <Link className={!isSent ? 'active' : ''} to="/messages">
            받은 쪽지 ({messages.length})
          </Link>
          <Link className={isSent ? 'active' : ''} to="/messages/sent">
            보낸 쪽지 ({sentMessages.length})
          </Link>
        </div>
        {isEmpty ? (
          <div className="empty-panel">쪽지가 없습니다.</div>
        ) : (
          visibleMessages.map(([name, time, title, team], index) => (
            <button
              className={index === selectedIndex ? 'message-item active' : 'message-item'}
              type="button"
              key={`${title}-${time}`}
              onClick={() => selectMessage(index)}
            >
              <strong>{title}</strong>
              <span>
                {isSent ? `받는 사람 · ${team} · ${time}` : `${name} · ${team} · ${time}`}
              </span>
            </button>
          ))
        )}
      </div>
      <article className="message-detail content-card">
        {selectedMessage ? (
          <>
            <h2>{selectedMessage[2]}</h2>
            <p>{isSent ? `보낸 사람: ${detail?.senderName ?? selectedMessage[0]}` : `보낸 사람: ${detail?.senderName ?? selectedMessage[0]}`}</p>
            <p>{isSent ? `받는 사람: ${detail?.receivedName ?? selectedMessage[3]}` : `받는 사람: ${detail?.receivedName ?? user?.name}`}</p>
            <time>{selectedMessage[1]}</time>
            <div className="message-body">
              {detail?.content || selectedMessage[4] || `${selectedMessage[2]} 관련 내용입니다.`}
              <br /><br />
              - 구분: {isSent ? '보낸 쪽지' : '받은 쪽지'}
              <br />
              - 담당 부서: {selectedMessage[3]}
              <br />
              - 처리 상태: {isSent ? '발송 완료' : '읽음'}
            </div>
            {!isSent ? (
              <button className="button primary" type="button" onClick={replyMessage}>
                답장
              </button>
            ) : null}
            <button className="button secondary" type="button" onClick={deleteMessage}>
              삭제
            </button>
            {error ? <p className="form-error">{error}</p> : null}
          </>
        ) : (
          <div className="empty-panel">
            {isSent ? '보낸 쪽지를 선택하면 내용이 표시됩니다.' : '쪽지를 선택하면 내용이 표시됩니다.'}
          </div>
        )}
      </article>
    </section>
  );
}

export default MessageInbox;
