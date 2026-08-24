import { Link, useSearchParams } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import { useApp } from '../context/AppContext.jsx';

function UserDetail() {
  const { lists } = useApp();
  const [searchParams] = useSearchParams();
  const index = Number.parseInt(searchParams.get('index') ?? '0', 10);
  const row = lists.users.rows[index] ?? lists.users.rows[0];

  if (!row) {
    return (
      <section className="detail-card">
        <p className="form-error">사용자 정보를 찾을 수 없습니다.</p>
        <Link className="button secondary" to="/users">
          목록
        </Link>
      </section>
    );
  }

  const fields = [
    ['이메일', row[5]],
    ['연락처', row[6]],
    ['부서', row[3]],
    ['권한', row[4]],
    ['등록일', row[8]],
  ];

  return (
    <section className="detail-card">
      <div className="profile-summary">
        <div className="profile-avatar">
          <Icon index={22} size={24.5} />
        </div>
        <div>
          <h2>{row[2]}</h2>
          <p className="mono">{row[1]}</p>
          <p>{row[3] || row[4]}</p>
          <span className="status-badge">
            <span />
            {row[7]}
          </span>
        </div>
      </div>
      <dl className="profile-grid">
        {fields.map(([label, value]) => (
          <div className="profile-field" key={label}>
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>
      <div className="roles">
        <p>보유 권한</p>
        <div>
          <span>{row._meta?.roles?.map((role) => role.roleCode).join(', ') || row[4]}</span>
        </div>
      </div>
      <div className="form-actions">
        <Link className="button secondary" to="/users">
          목록
        </Link>
        <Link className="button primary" to={`/users/new?index=${index}`}>
          수정
        </Link>
      </div>
    </section>
  );
}

export default UserDetail;
