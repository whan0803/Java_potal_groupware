import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';
import { getCurrentUserId } from '../services/backendAdapters.js';

const permissionKeys = [
  ['read', '조회(R)'],
  ['create', '등록(C)'],
  ['update', '수정(U)'],
  ['delete', '삭제(D)'],
];

function RoleMenuSettings() {
  const { user, lists, roleMenus, apiStatus, saveRoleMenus, refreshBackendState } = useApp();
  const [searchParams] = useSearchParams();
  const initialRole = searchParams.get('role') ?? lists.roles.rows[0]?.[1] ?? '';
  const [role, setRole] = useState(initialRole);
  const [rows, setRows] = useState(roleMenus[initialRole] ?? []);
  const [error, setError] = useState('');

  const selectedRoleRow = lists.roles.rows.find((roleRow) => roleRow[1] === role);
  const selectedRoleId = selectedRoleRow?._meta?.roleId;

  useEffect(() => {
    const firstRole = lists.roles.rows[0]?.[1];
    if (!role && firstRole) {
      setRole(firstRole);
      setRows(roleMenus[firstRole] ?? []);
    }
  }, [lists.roles.rows, role, roleMenus]);

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
    setRows(roleMenus[nextRole] ?? []);
  };

  const togglePermission = (rowIndex, key) => {
    setRows((current) =>
      current.map((row, index) => {
        if (index !== rowIndex) return row;
        const checked = !row[key];
        if (key === 'read' && !checked) {
          return { ...row, read: false, create: false, update: false, delete: false };
        }
        if (key !== 'read' && checked) {
          return { ...row, read: true, [key]: true };
        }
        return { ...row, [key]: checked };
      }),
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
      {!lists.roles.rows.length ? <p className="form-error">설정할 권한 데이터가 없습니다.</p> : null}
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
            {!rows.length ? (
              <tr>
                <td colSpan={permissionKeys.length + 2}>표시할 메뉴 권한이 없습니다.</td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
      <div className="form-actions">
        <button
          className="button primary"
          type="button"
          onClick={async () => {
            try {
              if (!role) {
                setError('권한을 먼저 선택하세요.');
                return;
              }
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
                await refreshBackendState();
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
