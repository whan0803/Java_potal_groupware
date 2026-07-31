import { Link, useLocation } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import { pageActions, pageMeta } from '../data/navigation.js';

function PageHeader() {
  const { pathname } = useLocation();
  const meta = pageMeta[pathname] ?? pageMeta['/'];
  const crumbs = meta.slice(0, -1);
  const title = meta.at(-1);
  const action = pageActions[pathname];

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
            <button className="button primary" type="button">
              <Icon index={21} size={12.25} />
              수정
            </button>
          </>
        ) : null}
        {action ? (
          <Link className="button primary" to={action[1]}>
            {action[0]}
          </Link>
        ) : null}
      </div>
    </section>
  );
}

export default PageHeader;
