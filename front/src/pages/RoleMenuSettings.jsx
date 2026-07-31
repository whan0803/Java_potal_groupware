import { useState } from 'react';

const initialRows = [
  { name: '사용자 목록', url: '/users/list', read: true, create: true, update: true, delete: true },
  { name: '사용자 등록', url: '/users/new', read: true, create: false, update: false, delete: false },
  { name: '권한 목록', url: '/roles/list', read: true, create: true, update: true, delete: false },
  { name: '권한 등록', url: '/roles/new', read: true, create: false, update: false, delete: false },
  { name: '권한별 메뉴 설정', url: '/roles/menu', read: false, create: false, update: false, delete: false },
  { name: '메뉴 목록', url: '/menus/list', read: true, create: true, update: true, delete: true },
  { name: '메뉴 등록·수정', url: '/menus/edit', read: true, create: true, update: true, delete: false },
  { name: '공지사항 목록', url: '/notices/list', read: true, create: false, update: false, delete: false },
  { name: '공지사항 등록', url: '/notices/new', read: true, create: true, update: true, delete: false },
  { name: '예약 목록', url: '/rsv/list', read: false, create: false, update: false, delete: false },
  { name: '예약 신청', url: '/rsv/new', read: false, create: false, update: false, delete: false },
  { name: '예약 승인', url: '/rsv/approve', read: false, create: false, update: false, delete: false },
  { name: '결재 대기함', url: '/apr/inbox', read: false, create: false, update: false, delete: false },
];

const permissionKeys = [
  ['read', '조회(R)'],
  ['create', '등록(C)'],
  ['update', '수정(U)'],
  ['delete', '삭제(D)'],
];

function RoleMenuSettings() {
  const [role, setRole] = useState('ROLE_ADMIN');
  const [rows, setRows] = useState(initialRows);

  const togglePermission = (rowIndex, key) => {
    setRows((current) =>
      current.map((row, index) => (index === rowIndex ? { ...row, [key]: !row[key] } : row)),
    );
  };

  return (
    <section className="content-card role-menu-card">
      <div className="role-menu-toolbar">
        <label>
          대상 권한
          <select value={role} onChange={(event) => setRole(event.target.value)}>
            <option value="ROLE_ADMIN">시스템 관리자 (ROLE_ADMIN)</option>
            <option value="ROLE_MANAGER">부서 관리자 (ROLE_MANAGER)</option>
            <option value="ROLE_USER">일반 사용자 (ROLE_USER)</option>
            <option value="ROLE_READONLY">읽기 전용 (ROLE_READONLY)</option>
          </select>
        </label>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>메뉴명</th>
              <th>URL</th>
              {permissionKeys.map(([, label]) => (
                <th key={label}>{label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, rowIndex) => (
              <tr key={row.url}>
                <td>{row.name}</td>
                <td>{row.url}</td>
                {permissionKeys.map(([key]) => (
                  <td className="checkbox-cell" key={`${row.url}-${key}`}>
                    <input
                      type="checkbox"
                      checked={row[key]}
                      onChange={() => togglePermission(rowIndex, key)}
                    />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="form-actions">
        <button
          className="button primary"
          type="button"
          onClick={() => window.alert(`${role} 권한별 메뉴 설정이 저장되었습니다.`)}
        >
          저장
        </button>
      </div>
    </section>
  );
}

export default RoleMenuSettings;
