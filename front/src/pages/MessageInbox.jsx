import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

function MessageInbox({ mode = 'inbox' }) {
  const { messages, sentMessages } = useApp();
  const isSent = mode === 'sent';
  const isEmpty = mode === 'empty';
  const visibleMessages = isSent ? sentMessages : messages;
  const [selectedIndex, setSelectedIndex] = useState(null);
  const selectedMessage = useMemo(() => {
    if (selectedIndex === null) return null;
    return visibleMessages[selectedIndex] ?? null;
  }, [selectedIndex, visibleMessages]);

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
              onClick={() => setSelectedIndex(index)}
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
            <p>{isSent ? `보낸 사람: ${selectedMessage[0]}` : `보낸 사람: ${selectedMessage[0]} (${selectedMessage[3]})`}</p>
            <p>{isSent ? `받는 사람: ${selectedMessage[3]}` : '받는 사람: 홍길동 (IT기획팀)'}</p>
            <time>{selectedMessage[1]}</time>
            <div className="message-body">
              {selectedMessage[4] || `${selectedMessage[2]} 관련 내용입니다.`}
              <br /><br />
              - 구분: {isSent ? '보낸 쪽지' : '받은 쪽지'}
              <br />
              - 담당 부서: {selectedMessage[3]}
              <br />
              - 처리 상태: {isSent ? '발송 완료' : '읽음'}
            </div>
            {!isSent ? (
              <button className="button primary" type="button">
                답장
              </button>
            ) : null}
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
