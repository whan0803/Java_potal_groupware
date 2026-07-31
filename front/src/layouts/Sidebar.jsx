import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { navGroups } from '../data/navigation.js';

function Sidebar() {
  const { pathname } = useLocation();
  const { user, logout } = useApp();
  const [openGroup, setOpenGroup] = useState(() => getActiveGroup(pathname));

  useEffect(() => {
    setOpenGroup(getActiveGroup(pathname));
  }, [pathname]);

  const toggleGroup = (label) => {
    setOpenGroup((current) => (current === label ? '' : label));
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div>
          <strong>기반 포털</strong>
          <span>관리시스템</span>
        </div>
      </div>
      <nav className="side-nav" aria-label="관리 메뉴">
        {navGroups.filter((group) => canShowGroup(group, user)).map((group) => (
          <div className="nav-group" key={group.label}>
            {group.path ? (
              <NavLink className={({ isActive }) => `nav-parent ${isActive ? 'active' : ''}`} to={group.path} end>
                <span>{group.label}</span>
              </NavLink>
            ) : (
              <button
                className={`nav-parent nav-toggle ${isActiveGroup(group, pathname) ? 'active' : ''}`}
                type="button"
                aria-expanded={openGroup === group.label}
                onClick={() => toggleGroup(group.label)}
              >
                <span>{group.label}</span>
              </button>
            )}
            {group.children ? (
              <div className={`nav-children ${openGroup === group.label ? 'open' : ''}`}>
                {group.children.map(([label, path]) => (
                  <NavLink className={({ isActive }) => `nav-child ${isActive ? 'active' : ''}`} to={path} key={path}>
                    {label}
                  </NavLink>
                ))}
              </div>
            ) : null}
          </div>
        ))}
      </nav>
      <div className="sidebar-user">
        <div className="user-strip">
          <div>
            <strong>{user?.name}</strong>
            <span>{user?.role}</span>
          </div>
        </div>
        <button className="logout-button" type="button" onClick={logout}>
          <span>로그아웃</span>
        </button>
      </div>
    </aside>
  );
}

function getActiveGroup(pathname) {
  return navGroups.find((group) => group.children?.some(([, path]) => path === pathname))?.label ?? '';
}

function isActiveGroup(group, pathname) {
  return group.children?.some(([, path]) => path === pathname);
}

function canShowGroup(group, user) {
  if (user?.role === '시스템 관리자') return true;
  return ['대시보드', '공지사항', '업무 관리', '일정 관리', '쪽지'].includes(group.label);
}

export default Sidebar;
