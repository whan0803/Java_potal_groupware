import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';

function Topbar() {
  const { user } = useApp();

  return (
    <header className="topbar">
      <div className="topbar-title">
        <strong>기반 포털 관리시스템</strong>
        <span>Groupware Administration Portal</span>
      </div>
      <div className="topbar-actions">
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

export default Topbar;
