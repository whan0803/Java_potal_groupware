import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';

function Topbar() {
  const { user, lists, messages } = useApp();
  const waitingReservations = lists.reservations.rows.filter((row) => row[8] === '대기').length;
  const waitingApprovals = lists.approval.rows.filter((row) => row[6] !== '완료').length;

  return (
    <header className="topbar">
      <div className="topbar-title">
        <strong>기반 포털 관리시스템</strong>
        <span>Groupware Administration Portal</span>
      </div>
      <div className="topbar-actions">
        <HeaderAction icon={0} count={String(waitingReservations)} tone="yellow" />
        <HeaderAction icon={1} count={String(waitingApprovals)} tone="red" />
        <HeaderAction icon={2} count={String(messages.length)} tone="pink" />
        <HeaderAction icon={3} />
        <div className="topbar-user">
          <span>{user?.name}</span>
          <div className="top-avatar">
            <Icon index={4} size={12.25} />
          </div>
        </div>
      </div>
    </header>
  );
}

function HeaderAction({ icon, count, tone }) {
  return (
    <button className="header-icon-button" type="button">
      <Icon index={icon} />
      {Number(count) > 0 ? <span className={`notification-dot ${tone}`}>{count}</span> : null}
    </button>
  );
}

export default Topbar;
