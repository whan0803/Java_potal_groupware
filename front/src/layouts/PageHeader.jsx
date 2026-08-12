import { Link, useLocation } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';
import { pageActions, pageMeta } from '../data/navigation.js';
import { canUsePermission } from '../utils/permissions.js';

function PageHeader() {
  const { pathname, search } = useLocation();
  const { user, permissions } = useApp();
  const meta = pageMeta[pathname] ?? pageMeta['/'];
  const crumbs = meta.slice(0, -1);
  const title = meta.at(-1);
  const action = pageActions[pathname];
  const actions = Array.isArray(action?.[0]) ? action : action ? [action] : [];
  const canCreate = canUsePermission(user, permissions, pathname, 'create');
  const canUpdate = canUsePermission(user, permissions, pathname, 'update');

  return (
    <section className="page-header">
      <div>
        <div className="breadcrumbs">
          {[...crumbs, title].map((crumb, index, items) => (
            <span className="crumb" key={`${crumb}-${index}`}>
              {crumb}
              {index < items.length - 1 ? <Icon index={19} size={10.5} /> : null}
            </span>
          ))}
        </div>
        <h1>{title}</h1>
      </div>
      <div className="header-buttons">
        {pathname === '/users/detail' ? (
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
        {canCreate
          ? actions.map(([label, to], index) => (
              <Link className={index === 0 ? 'button primary' : 'button secondary'} to={to} key={to}>
                {label}
              </Link>
            ))
          : null}
      </div>
    </section>
  );
}

export default PageHeader;
