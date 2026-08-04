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
    const rows = filterRowsByTab(config, activeTab);
    if (!query.trim()) return rows;
    return rows.filter((row) => row.some((cell) => String(cell).includes(query.trim())));
  }, [config, activeTab, query]);

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

function filterRowsByTab(config, activeTab) {
  if (!config.tabs) return config.rows;
  const tab = config.tabs[activeTab] ?? '';
  const statusIndex = config.columns.findIndex((column) => ['상태', '결재 상태', '사용여부'].includes(column));
  if (statusIndex < 0) return config.rows;
  if (tab.includes('대기')) return config.rows.filter((row) => ['진행중', '대기'].includes(row[statusIndex]));
  if (tab.includes('완료')) return config.rows.filter((row) => row[statusIndex] === '완료');
  if (tab.includes('사용')) return config.rows.filter((row) => row[statusIndex] === '사용');
  if (tab.includes('미사용')) return config.rows.filter((row) => row[statusIndex] === '미사용');
  return config.rows;
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
    navigate(`/roles/new?index=${rowIndex}`);
    return;
  }

  const path = getActionPath(listKey, action);
  navigate(`${path}?index=${rowIndex}`);
}

function getActionPath(listKey, action) {
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

  if (['수정', '상세'].includes(action)) {
    const editPaths = {
      menus: '/menus/edit',
      notices: '/notices/new',
      boards: '/boards/new',
      posts: '/posts/new',
      reservations: '/reservations/approve',
      templates: '/templates/new',
      tasks: '/tasks/new',
      codes: '/codes/new',
    };
    return editPaths[listKey] ?? paths[listKey] ?? '/';
  }

  return paths[listKey] ?? '/';
}

export default ListPage;
