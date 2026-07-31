import DataTable from './DataTable.jsx';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

function ListPage({ listKey }) {
  const navigate = useNavigate();
  const { lists, updateRowStatus, removeRow } = useApp();
  const config = lists[listKey];
  const [activeTab, setActiveTab] = useState(0);
  const [query, setQuery] = useState('');
  const filteredRows = useMemo(() => {
    if (!query.trim()) return config.rows;
    return config.rows.filter((row) => row.some((cell) => String(cell).includes(query.trim())));
  }, [config.rows, query]);

  return (
    <section className="content-card list-card">
      <div className="toolbar">
        {config.tabs ? (
          <div className="tabs">
            {config.tabs.map((tab, index) => (
              <button
                className={index === activeTab ? 'active' : ''}
                type="button"
                key={tab}
                onClick={() => setActiveTab(index)}
              >
                {tab}
              </button>
            ))}
          </div>
        ) : (
          <input
            type="search"
            placeholder={config.search}
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        )}
        <span>총 {filteredRows.length}건</span>
      </div>
      <DataTable
        columns={config.columns}
        rows={filteredRows}
        listKey={listKey}
        onAction={(row, action) => {
          const rowIndex = config.rows.indexOf(row);
          handleTableAction({ listKey, action, rowIndex, navigate, updateRowStatus, removeRow });
        }}
      />
    </section>
  );
}

function handleTableAction({ listKey, action, rowIndex, navigate, updateRowStatus, removeRow }) {
  if (action === '삭제') {
    removeRow(listKey, rowIndex);
    return;
  }

  if (['결재 처리', '승인 / 반려'].includes(action)) {
    updateRowStatus(listKey, rowIndex, '완료');
    window.alert('처리되었습니다.');
    return;
  }

  if (listKey === 'roles' && action === '수정') {
    navigate('/roles/new');
    return;
  }

  navigate(getActionPath(listKey));
}

function getActionPath(listKey) {
  const paths = {
    users: '/users/detail',
    roles: '/roles/detail',
    menus: '/menus/edit',
    notices: '/notices/new',
    boards: '/boards/new',
    posts: '/posts/new',
    reservations: '/reservations/approve',
    approval: '/approval',
    templates: '/templates/new',
    tasks: '/tasks/new',
    codes: '/codes/new',
    logs: '/logs',
  };

  return paths[listKey] ?? '/';
}

export default ListPage;
