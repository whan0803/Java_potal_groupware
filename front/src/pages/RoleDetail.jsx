import { Link } from 'react-router-dom';

const permissionRows = [
  ['사용자 목록', '/users/list', true, true, true, true],
  ['사용자 등록', '/users/new', true, true, false, false],
  ['권한 목록', '/roles/list', true, true, true, true],
  ['권한 등록', '/roles/new', true, true, true, true],
  ['권한별 메뉴 설정', '/roles/menu', true, true, true, true],
  ['메뉴 목록', '/menus/list', true, true, true, true],
];

function RoleDetail() {
  return (
    <div className="role-detail-grid">
      <section className="content-card role-info-card">
        <h2>권한 정보</h2>
        <dl>
          <dt>권한 코드</dt>
          <dd>ROLE_ADMIN</dd>
          <dt>권한명</dt>
          <dd>시스템 관리자</dd>
          <dt>설명</dt>
          <dd>시스템 전체 관리 권한</dd>
          <dt>사용자 수</dt>
          <dd>2명</dd>
          <dt>사용여부</dt>
          <dd>
            <span className="pill 사용">사용</span>
          </dd>
          <dt>등록일</dt>
          <dd>2024-01-01</dd>
        </dl>
      </section>
      <section className="content-card role-permission-card">
        <div className="card-title">
          <h2>설정된 메뉴 권한</h2>
          <div className="header-buttons">
            <Link className="button secondary" to="/roles">
              목록
            </Link>
            <Link className="button primary" to="/roles/menu">
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
            {permissionRows.map((row) => (
              <tr key={row[1]}>
                <td>{row[0]}</td>
                <td>{row[1]}</td>
                {row.slice(2).map((enabled, index) => (
                  <td className={enabled ? 'permission-on' : 'permission-off'} key={`${row[1]}-${index}`}>
                    {enabled ? '✓' : '×'}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default RoleDetail;
