import DataTable from './DataTable.jsx';
import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';
import { canUsePermission, isAdminUser } from '../utils/permissions.js';

function ListPage({ listKey }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { user, permissions, lists, updateRowStatus, removeRow, refreshBackendState } = useApp();
  const config = lists[listKey];
  const [activeTab, setActiveTab] = useState(0);
  const [query, setQuery] = useState('');
  const canUpdate = canUsePermission(user, permissions, pathname, 'update');
  const canDelete = canUsePermission(user, permissions, pathname, 'delete');
  const effectiveCanUpdate = canUpdate || ['tasks', 'reservations'].includes(listKey) || (listKey === 'approval' && isAdminUser(user));
  const effectiveCanDelete = canDelete || ['tasks', 'reservations'].includes(listKey);
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
        canUpdate={effectiveCanUpdate}
        canDelete={effectiveCanDelete}
        canEditRow={(row) => canEditRow({ listKey, row, user })}
        onAction={async (row, action) => {
          const rowIndex = config.rows.indexOf(row);
          try {
            await handleTableAction({ listKey, action, row, rowIndex, user, navigate, updateRowStatus, removeRow, refreshBackendState });
          } catch (error) {
            window.alert(error.message || '처리 중 오류가 발생했습니다.');
          }
        }}
      />
    </section>
  );
}

function canEditRow({ listKey, row, user }) {
  if (!['tasks', 'posts', 'reservations'].includes(listKey)) return true;
  if (isAdminUser(user)) return true;
  if (listKey === 'posts') return Number(row?._meta?.writerId) === Number(user?.userId);
  if (listKey === 'reservations') return Number(row?._meta?.requesterId) === Number(user?.userId);
  return Number(row?._meta?.requesterId) === Number(user?.userId);
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

async function handleTableAction({ listKey, action, row, rowIndex, user, navigate, updateRowStatus, removeRow, refreshBackendState }) {
  if (listKey === 'templates' && action === '비활성화') {
    if (!window.confirm('이 문서양식을 비활성화하시겠습니까?')) return;
    await updateRowStatus('templates', rowIndex, '미사용');
    window.alert('문서양식이 비활성화되었습니다.');
    return;
  }

  if (listKey === 'templates' && action === '삭제') {
    if (!window.confirm('이 문서양식을 완전히 삭제하시겠습니까? 사용 중인 양식은 삭제할 수 없습니다.')) return;
    await removeRow(listKey, rowIndex);
    window.alert('문서양식이 삭제되었습니다.');
    return;
  }

  if (action === '삭제') {
    await removeRow(listKey, rowIndex);
    return;
  }

  if (listKey === 'approval' && ['승인', '반려', '결재 처리'].includes(action)) {
    const approve = action !== '반려';
    if (approve && !window.confirm('이 결재 문서를 승인하시겠습니까?')) return;

    const comment = approve ? '' : window.prompt('반려 사유를 입력하세요.', '');
    if (!approve && comment === null) return;
    if (!approve && !comment.trim()) {
      window.alert('반려 사유를 입력해야 반려 처리할 수 있습니다.');
      return;
    }

    await api.patch(`/api/approvals/${row._meta.approvalDocumentId}/${approve ? 'approve' : 'reject'}`, {
      approverId: user.userId,
      comment,
    });
    await refreshBackendState();
    window.alert(approve ? '승인되었습니다.' : '반려되었습니다.');
    return;
  }

  if (['승인 / 반려'].includes(action)) {
    await updateRowStatus(listKey, rowIndex, '완료');
    window.alert('처리되었습니다.');
    return;
  }

  if (listKey === 'roles' && action === '수정') {
    navigate(`/roles/new?index=${rowIndex}`);
    return;
  }

  if (listKey === 'tasks' && action === '진행률') {
    const value = window.prompt('진행률을 0~100 사이 숫자로 입력하세요.', String(row?._meta?.progressRate ?? 0));
    if (value === null) return;
    const progressRate = Number(value);
    if (!Number.isInteger(progressRate) || progressRate < 0 || progressRate > 100) {
      window.alert('진행률은 0~100 사이 숫자여야 합니다.');
      return;
    }
    await api.patch(`/api/tasks/${row._meta.taskId}/progress`, { progressRate });
    await refreshBackendState();
    return;
  }

  if (listKey === 'tasks' && action === '상태') {
    const value = window.prompt('상태를 입력하세요: 예정, 진행중, 완료, 보류', row?.[5] ?? '예정');
    if (value === null) return;
    if (!['예정', '진행중', '완료', '보류'].includes(value)) {
      window.alert('상태는 예정, 진행중, 완료, 보류 중 하나여야 합니다.');
      return;
    }
    await updateRowStatus('tasks', rowIndex, value);
    return;
  }

  if (listKey === 'reservations' && action === '수정') {
    navigate(`/reservations/new?index=${rowIndex}`);
    return;
  }

  if (listKey === 'reservations' && action === '취소') {
    await removeRow('reservations', rowIndex);
    return;
  }

  if (listKey === 'posts') {
    navigate(action === '수정' ? `/posts/new?index=${rowIndex}` : `/posts/detail?index=${rowIndex}`);
    return;
  }

  if (listKey === 'templates' && action === '상세') {
    navigate(`/templates/detail?index=${rowIndex}`);
    return;
  }

  if (listKey === 'templates' && action === '수정') {
    navigate(`/templates/new?index=${rowIndex}`);
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
