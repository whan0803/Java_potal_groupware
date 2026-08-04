import { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext.jsx';

function RoleSelectModal({ selectedRoles, onClose, onConfirm }) {
  const { lists } = useApp();
  const [query, setQuery] = useState('');
  const [checkedRoles, setCheckedRoles] = useState(selectedRoles);
  const roles = useMemo(
    () => lists.roles.rows.map((row) => ({ code: row[1], name: row[2], enabled: row[5] === '사용' })),
    [lists.roles.rows],
  );
  const filteredRoles = useMemo(
    () =>
      roles.filter(
        (role) => role.enabled && `${role.code} ${role.name}`.toLowerCase().includes(query.toLowerCase()),
      ),
    [query, roles],
  );

  const toggleRole = (code) => {
    setCheckedRoles((current) =>
      current.includes(code) ? current.filter((roleCode) => roleCode !== code) : [...current, code],
    );
  };

  return (
    <div className="modal-backdrop">
      <section className="role-modal" role="dialog" aria-modal="true" aria-labelledby="role-modal-title">
        <header>
          <h2 id="role-modal-title">권한 선택 (USR-005)</h2>
          <button type="button" onClick={onClose} aria-label="닫기">
            ×
          </button>
        </header>
        <div className="role-modal-body">
          <input
            className="modal-search"
            placeholder="권한코드 · 권한명 검색"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="role-option-list">
            {filteredRoles.map((role) => (
              <label className="role-option" key={role.code}>
                <input
                  type="checkbox"
                  checked={checkedRoles.includes(role.code)}
                  onChange={() => toggleRole(role.code)}
                />
                <span>
                  <strong>{role.code}</strong>
                  <small>{role.name}</small>
                </span>
              </label>
            ))}
          </div>
        </div>
        <footer>
          <button className="button secondary" type="button" onClick={onClose}>
            취소
          </button>
          <button className="button primary" type="button" onClick={() => onConfirm(checkedRoles)}>
            선택 완료
          </button>
        </footer>
      </section>
    </div>
  );
}

export default RoleSelectModal;
