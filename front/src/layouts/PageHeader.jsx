import { Link, useLocation } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';
import { pageActions, pageMeta } from '../data/navigation.js';
import { canOpenAction, canUsePermission } from '../utils/permissions.js';

function PageHeader() {
  //현재 경로 및 퀴리스트링 가져오기 ex)/users/detail?id=3
  const { pathname, search } = useLocation(); //pathname = /users/detail, search = ?id=3
  const { user, permissions } = useApp();
  //breadcrumb 정보 가져옴
  const meta = pageMeta[pathname] ?? pageMeta["/"]; //pageMeta['/users/detail'] = ['사용자 관리', '사용자 상세']
  const crumbs = meta.slice(0, -1); //crumbs = ["사용자 관리"]
  const title = meta.at(-1); //  title = "사용자 상세"
  //pageActions가 단일 액션이든 여러 액션이든 동일하게 처리하려는 코드
  //pageActions['/users'] = ['등록', '/users/new']
  //pageActions['/users'] = [
  //   ['등록', '/users/new'],
  //   ['엑셀 다운로드', '/users/export']
  // ]
  const action = pageActions[pathname];
  const actions = Array.isArray(action?.[0]) ? action : action ? [action] : [];

  const canUpdate = canUsePermission(user, permissions, pathname, "update");

  return (
    <section className="page-header">
      <div>
        <div className="breadcrumbs">
          {[...crumbs, title].map((crumb, index, items) => (
            <span className="crumb" key={`${crumb}-${index}`}>
              {crumb}
              {index < items.length - 1 ? (
                <Icon index={19} size={10.5} />
              ) : null}
            </span>
          ))}
        </div>
        <h1>{title}</h1>
      </div>
      <div className="header-buttons">
        {pathname === "/users/detail" ? (
          <>
            <Link className="button secondary" to="/users">
              <Icon index={20} size={12.25} />
              목록
            </Link>
            {canUpdate ? (
              <Link className="button primary" to={`/users/new${search}`}>
                <Icon index={21} size={12.25} />
                수정
              </Link>
            ) : null}
          </>
        ) : null}
        {actions
          .filter(([, to]) => canOpenAction(user, permissions, to))
          .map(([label, to], index) => (
            <Link
              className={index === 0 ? "button primary" : "button secondary"}
              to={to}
              key={to}
            >
              {label}
            </Link>
          ))}
      </div>
    </section>
  );
}

export default PageHeader;
