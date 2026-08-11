import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';
import { getCurrentUserId } from '../services/backendAdapters.js';

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
  const { user, lists, roleMenus, apiStatus, saveRoleMenus } = useApp();
  const [searchParams] = useSearchParams();
  const initialRole = searchParams.get('role') ?? lists.roles.rows[0]?.[1] ?? 'ROLE_ADMIN';
  const [role, setRole] = useState(initialRole);
  const [rows, setRows] = useState(roleMenus[initialRole] ?? initialRows);
  const [error, setError] = useState('');

  const selectedRoleRow = lists.roles.rows.find((roleRow) => roleRow[1] === role);
  const selectedRoleId = selectedRoleRow?._meta?.roleId;

  useEffect(() => {
    if (!apiStatus.connected || !selectedRoleId) return;
    api.get(`/api/roles/${selectedRoleId}/menus`)
      .then((items) => {
        setRows(items.map((item) => ({
          menuId: item.menuId,
          name: item.menuName,
          url: item.menuUrl ?? '',
          read: item.readYn === 'Y',
          create: item.createYn === 'Y',
          update: item.updateYn === 'Y',
          delete: item.deleteYn === 'Y',
        })));
        setError('');
      })
      .catch((fetchError) => setError(fetchError.message || '권한별 메뉴를 불러오지 못했습니다.'));
  }, [apiStatus.connected, selectedRoleId]);

  const handleRoleChange = (nextRole) => {
    setRole(nextRole);
    setRows(roleMenus[nextRole] ?? initialRows);
  };

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
          <select value={role} onChange={(event) => handleRoleChange(event.target.value)}>
            {lists.roles.rows.map((roleRow) => (
              <option value={roleRow[1]} key={roleRow[1]}>
                {roleRow[2]} ({roleRow[1]})
              </option>
            ))}
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
          onClick={async () => {
            try {
              if (apiStatus.connected && selectedRoleId) {
                await api.put(`/api/roles/${selectedRoleId}/menus`, {
                  userId: getCurrentUserId(user),
                  menus: rows
                    .filter((row) => row.menuId)
                    .map((row) => ({
                      menuId: row.menuId,
                      readYn: row.read ? 'Y' : 'N',
                      createYn: row.create ? 'Y' : 'N',
                      updateYn: row.update ? 'Y' : 'N',
                      deleteYn: row.delete ? 'Y' : 'N',
                    })),
                });
              } else {
                saveRoleMenus(role, rows);
              }
              setError('');
              window.alert(`${role} 권한별 메뉴 설정이 저장되었습니다.`);
            } catch (saveError) {
              setError(saveError.message || '저장 중 오류가 발생했습니다.');
            }
          }}
        >
          저장
        </button>
      </div>
      {error ? <p className="form-error">{error}</p> : null}
    </section>
  );
}

export default RoleMenuSettings;
