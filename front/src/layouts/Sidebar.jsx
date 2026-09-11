import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';
import { navGroups } from '../data/navigation.js';

function Sidebar() {
  const { pathname } = useLocation();
  const { user, logout } = useApp();
  
  const [openGroups, setOpenGroups] = useState(() => { //openGroup -> 현재 열려있는 드롭다운 메뉴 그룹 목록
    //예를 들어 /user/new가 열려 있으면 getActiveGroup(pathname)이 "사용자 등록"아 된다
    const activeGroup = getActiveGroup(pathname);
    return activeGroup ? new Set([activeGroup]) : new Set(); //여기서 Set을 쓴 이유는 같은 그룹이 중복으로 들어가지 않게 하기 위해 Set을 사용
  });

  //이부분은 url이 바뀔때마다 실행
  useEffect(() => {
    const activeGroup = getActiveGroup(pathname);
    if (!activeGroup) return;

    setOpenGroups((current) => {
      const next = new Set(current);
      next.add(activeGroup);
      return next;
    });
  }, [pathname]);

  //드롭다운 클릭 함수
  const toggleGroup = (label) => {
    setOpenGroups((current) => {
      const next = new Set(current); //새 Set을 만든 이유 -> React가 변경을 제대로 감지하게 할려고 함. 객체를 직접 바꾸는 것보다 새 객체를 만들어 반환하는 방식이 안전함

      if (next.has(label)) { //오픈그룹 안에 이름이 있냐
        next.delete(label); //닫기
      } else {
        next.add(label);// 열기
      }

      return next;
    });
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div>
          <strong>포털 그룹웨어</strong>
          <span>관리시스템</span>
        </div>
      </div>
      <nav className="side-nav" aria-label="관리 메뉴">
        {navGroups.map((group) => (
          <div className="nav-group" key={group.label}>
            {group.path ? (
              <NavLink className={({ isActive }) => `nav-parent ${isActive ? 'active' : ''}`} to={group.path} end>
                <Icon index={group.icon} size={12.25} />
                <span className="nav-label">{group.label}</span>
              </NavLink>
            ) : (
              <button
                className={`nav-parent nav-toggle ${isActiveGroup(group, pathname) ? 'active' : ''}`}
                type="button"
                aria-expanded={openGroups.has(group.label)}
                onClick={() => toggleGroup(group.label)}
              >
                <Icon index={group.icon} size={12.25} />
                <span className="nav-label">{group.label}</span>
                <span className="nav-chevron" aria-hidden="true" />
              </button>
            )}
            {group.children ? (
              <div className={`nav-children ${openGroups.has(group.label) ? 'open' : ''}`}>
                {group.children.map(([label, path]) => (
                  <NavLink className={({ isActive }) => `nav-child ${isActive ? 'active' : ''}`} to={path} key={path} end>
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

//현재 경로가 어느 그룹에 속하는지 판닺ㄴ.
function getActiveGroup(pathname) {
  return navGroups.find((group) => group.children?.some(([, path]) => isCurrentPath(pathname, path)))?.label ?? '';
}

//url이 어떤 그룹의 child path와 맞는지 찾고, 맞으면 그 그룹으로 반환
function isActiveGroup(group, pathname) {
  return group.children?.some(([, path]) => isCurrentPath(pathname, path));
}


//현재 그룹이 active인지 확인
function isCurrentPath(pathname, path) {
  return pathname === path || pathname.startsWith(`${path}/`);
}

export default Sidebar;

// 1. 메뉴 데이터와 화면 코드를 분리하려고
//    메뉴 목록은 navigation.js에 두고, Sidebar.jsx는 그 데이터를 그리기만 합니다. 유지보수가 쉬워진다
// 2. 현재 페이지 위치를 자동으로 보여주려고
//    사용자가 /roles/menu에 들어가면 "권한 관리" 드롭다운이 자동으로 열려 있어야 한다
// 3. 사용자 클릭 상태를 유지하려고
//    사용자가 여러 메뉴를 열어둘 수 있고, 페이지 이동을 해도 기존에 열어둔 그룹을 최대한 유지합니다.
// 한 줄로 정리하면, 이 Sidebar.jsx는 현재 URL을 기준으로 active 메뉴를 표시하고, 사용자가 클릭한 드롭다운 상태를 React state로 관리하는 사이드바 컴포넌트이다
