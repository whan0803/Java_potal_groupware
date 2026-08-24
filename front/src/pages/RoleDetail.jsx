import { Link, useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

function RoleDetail() {
  const { lists, roleMenus } = useApp();
  const [searchParams] = useSearchParams();
  const index = Number.parseInt(searchParams.get('index') ?? '0', 10);
  const row = lists.roles.rows[index] ?? lists.roles.rows[0];

  if (!row) {
    return (
      <section className="detail-card">
        <p className="form-error">권한 정보를 찾을 수 없습니다.</p>
        <Link className="button secondary" to="/roles">
          목록
        </Link>
      </section>
    );
  }

  const roleCode = row[1];
  const permissionRows = roleMenus[roleCode] ?? [];

  return (
    <div className="role-detail-grid">
      <section className="content-card role-info-card">
        <h2>권한 정보</h2>
        <dl>
          <dt>권한 코드</dt>
          <dd>{row[1]}</dd>
          <dt>권한명</dt>
          <dd>{row[2]}</dd>
          <dt>설명</dt>
          <dd>{row[3]}</dd>
          <dt>사용자 수</dt>
          <dd>{row[4]}</dd>
          <dt>사용여부</dt>
          <dd>
            <span className={`pill ${row[5]}`}>{row[5]}</span>
          </dd>
          <dt>등록일</dt>
          <dd>{row[6]}</dd>
        </dl>
      </section>
      <section className="content-card role-permission-card">
        <div className="card-title">
          <h2>설정된 메뉴 권한</h2>
          <div className="header-buttons">
            <Link className="button secondary" to="/roles">
              목록
            </Link>
            <Link className="button primary" to={`/roles/menu?role=${roleCode}`}>
              수정
            </Link>
          </div>
        </div>
        <table>
          <thead>
            <tr>
              <th>메뉴명</th>
              <th>URL</th>
              <th>조회</th>
              <th>등록</th>
              <th>수정</th>
              <th>삭제</th>
            </tr>
          </thead>
          <tbody>
            {permissionRows.map((permission) => (
              <tr key={permission.url}>
                <td>{permission.name}</td>
                <td>{permission.url}</td>
                {['read', 'create', 'update', 'delete'].map((key) => {
                  const enabled = permission[key];
                  return (
                  <td className={enabled ? 'permission-on' : 'permission-off'} key={`${permission.url}-${key}`}>
                    {enabled ? '✓' : '×'}
                  </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default RoleDetail;
