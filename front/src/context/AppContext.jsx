import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { listScreens } from '../data/listScreens.js';
import { api, authApi } from '../services/api.js';
import {
  deleteBackendRow,
  fetchBackendState,
  saveFormToBackend,
  updateBackendStatus,
} from '../services/backendAdapters.js';
import { loadStorage, saveStorage } from '../utils/storage.js';

const AuthContext = createContext(null);
const GroupwareDataContext = createContext(null);
const GroupwareActionsContext = createContext(null);

const formAuditTargets = {
  userRegister: 'users',
  roleRegister: 'roles',
  menuEdit: 'menus',
  noticeRegister: 'notices',
  boardRegister: 'boards',
  postRegister: 'posts',
  reservationRegister: 'reservations',
  resourceRegister: 'reservation_resources',
  templateRegister: 'templates',
  taskRegister: 'tasks',
  scheduleRegister: 'schedules',
  messageCompose: 'messages',
  codeRegister: 'codes',
};

const formAuditIdKeys = {
  userRegister: 'userId',
  roleRegister: 'roleId',
  menuEdit: 'menuId',
  noticeRegister: 'noticeId',
  boardRegister: 'boardId',
  postRegister: 'postId',
  reservationRegister: 'reservationId',
  resourceRegister: 'resourceId',
  templateRegister: 'templateId',
  taskRegister: 'taskId',
  scheduleRegister: 'scheduleId',
  codeRegister: 'codeGroupId',
};

const adminTestAccounts = [
  { id: 'admin', password: 'admin123', name: '관리자', role: '시스템 관리자', enabled: true },
];

const displayRoleCode = (roleCode) => String(roleCode ?? '').replace(/^ROLE_/, '');

const toAuditData = (data) => {
  if (data == null) return null;
  try {
    return JSON.parse(JSON.stringify(data));
  } catch {
    return { value: String(data) };
  }
};

const getAuditRecordId = (formKey, editingRow) => {
  const idKey = formAuditIdKeys[formKey];
  return Number(editingRow?._meta?.[idKey]) || 0;
};

const toUserSession = (response) => ({
  userId: response.userId,
  id: response.loginId,
  name: response.userName,
  role: displayRoleCode(response.roles?.[0]) || '사용자',
  roles: response.roles ?? [],
});

export function AppProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accounts, setAccounts] = useState(adminTestAccounts);
  const [apiStatus, setApiStatus] = useState({ connected: false, loading: false, error: '' });

  const [lists, setLists] = useState(listScreens);
  const [schedules, setSchedules] = useState(() => loadStorage('schedules', []));
  const [messages, setMessages] = useState([]);
  const [sentMessages, setSentMessages] = useState([]);
  const [roleMenus, setRoleMenus] = useState({});
  const [resources, setResources] = useState(() => loadStorage('resources', []));
  const [dashboard, setDashboard] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [permissionsLoaded, setPermissionsLoaded] = useState(false);


  const writeAuditLog = async (tableName, actionType, detail, auditData = {}) => {
    const beforeData = auditData.beforeData === undefined ? null : toAuditData(auditData.beforeData);
    const afterData = auditData.afterData === undefined ? { detail } : toAuditData(auditData.afterData);

    if (apiStatus.connected && user?.userId) {
      try {
        await api.post('/api/audit-logs', {
          tableName,
          recordId: auditData.recordId ?? 0,
          actionType,
          beforeData,
          afterData,
          actorId: user.userId,
        });
      } catch (error) {
        console.error('감사로그 저장에 실패했습니다.', error);
      }
    }

    setLists((current) => {
      const target = current.logs;
      const nextRows = [
        String(target.rows.length + 1),
        tableName,
        actionType,
        user?.name ?? 'system',
        new Date().toLocaleString('ko-KR'),
        detail,
        '상세',
      ];

      return {
        ...current,
        logs: {
          ...target,
          rows: [nextRows, ...target.rows],
          total: `총 ${target.rows.length + 1}건`,
        },
      };
    });
  };

  const resetSessionData = () => {
    setDashboard(null);
    setPermissions([]);
    setPermissionsLoaded(false);
  };

  //dashboard 데이터 가져오기
  const refreshBackendState = async () => {
    if (!user) return;
    setApiStatus((current) => ({ ...current, loading: true, error: '' }));
    try {
      const session = await authApi.me();
      const sessionUser = toUserSession(session);
      setUser(sessionUser);
      const state = await fetchBackendState(sessionUser);
      setLists((current) => ({ ...current, ...state.lists }));
      setSchedules(state.schedules);
      setMessages(state.messages);
      setSentMessages(state.sentMessages);
      setResources(state.resources);
      setDashboard(state.dashboard);
      setPermissions(state.permissions);
      setPermissionsLoaded(true);
      setApiStatus({ connected: true, loading: false, error: '' });
    } catch (error) {
      setPermissionsLoaded(true);
      setApiStatus({ connected: false, loading: false, error: error.message });
    }
  };

  useEffect(() => {
    refreshBackendState();
  }, [user?.userId, user?.id]);

  const login = async ({ id, password }) => {
    try {
      await authApi.login({ id, password });
      const session = await authApi.me();
      setPermissionsLoaded(false);
      setUser(toUserSession(session));
      setApiStatus({ connected: true, loading: true, error: '' });
      return { ok: true };
    } catch (error) {
      if (!error.status) {
        const localResult = localLogin({ id, password });
        if (localResult.ok) {
          setApiStatus({ connected: false, loading: false, error: error.message });
          return localResult;
        }
      }
      setUser(null);
      setApiStatus({ connected: false, loading: false, error: error.message });
      return { ok: false, message: error.message || '로그인에 실패했습니다.' };
    }
  };

  const localLogin = ({ id, password }) => {
    const account = accounts.find((item) => item.id === id);
    if (!account) return { ok: false, message: '존재하지 않는 사용자입니다.' };
    if (account.password !== password) return { ok: false, message: '비밀번호가 일치하지 않습니다.' };
    if (!account.enabled) return { ok: false, message: '사용 중지된 계정입니다.' };

    setUser({ id: account.id, name: account.name, role: account.role });
    return { ok: true };
  };

  const logout = async () => {
    if (apiStatus.connected) {
      try {
        await authApi.logout();
      } catch {
        // The local session should still be cleared even if the server session is already gone.
      }
    }
    setUser(null);
    resetSessionData();
  };

  const changePassword = async ({ currentPassword, nextPassword, confirmPassword }) => {
    if (!currentPassword || !nextPassword || !confirmPassword) {
      return { ok: false, message: '모든 항목을 입력하세요.' };
    }
    if (nextPassword !== confirmPassword) {
      return { ok: false, message: '새 비밀번호와 확인 값이 다릅니다.' };
    }
    if (nextPassword.length < 6) {
      return { ok: false, message: '새 비밀번호는 6자 이상이어야 합니다.' };
    }
    if (apiStatus.connected) {
      try {
        const response = await authApi.changePassword({ currentPassword, nextPassword, confirmPassword });
        await writeAuditLog('users', 'PASSWORD_CHANGE', `${user?.id} 비밀번호 변경`);
        await refreshBackendState();
        return { ok: true, message: response.message ?? '비밀번호가 변경되었습니다.' };
      } catch (error) {
        return { ok: false, message: error.message || '비밀번호 변경에 실패했습니다.' };
      }
    }

    const account = accounts.find((item) => item.id === user?.id);
    if (account?.password !== currentPassword) {
      return { ok: false, message: '기존 비밀번호가 일치하지 않습니다.' };
    }
    setAccounts((current) =>
      current.map((item) => (item.id === user?.id ? { ...item, password: nextPassword } : item)),
    );
    writeAuditLog('users', 'PASSWORD_CHANGE', `${user?.id} 비밀번호 변경`);
    return { ok: true, message: '비밀번호가 변경되었습니다.' };
  };

  const addListRow = (listKey, row) => {
    setLists((current) => {
      const target = current[listKey];
      if (!target) return current;

      const nextRows = [...target.rows, row];
      return {
        ...current,
        [listKey]: {
          ...target,
          rows: nextRows,
          total: `총 ${nextRows.length}건`,
        },
      };
    });
    writeAuditLog(listKey, 'CREATE', `${listKey} 데이터 등록`);
  };

  const updateListRow = (listKey, rowIndex, row) => {
    setLists((current) => {
      const target = current[listKey];
      if (!target) return current;
      const rows = target.rows.map((item, index) => (index === rowIndex ? row : item));

      return {
        ...current,
        [listKey]: { ...target, rows, total: `총 ${rows.length}건` },
      };
    });
    writeAuditLog(listKey, 'UPDATE', `${listKey} 데이터 수정`);
  };

  const updateRowStatus = async (listKey, rowIndex, status = '미사용') => {
    const row = lists[listKey]?.rows[rowIndex];
    if (apiStatus.connected && row?._meta) {
      await updateBackendStatus(listKey, row, status, user);
      await writeAuditLog(listKey, 'UPDATE', `${listKey} 상태 변경`, {
        recordId: row._meta[`${listKey.slice(0, -1)}Id`] ?? row._meta.id ?? 0,
        beforeData: row._meta,
        afterData: { ...row._meta, status },
      });
      await refreshBackendState();
      return;
    }
    setLists((current) => {
      const target = current[listKey];
      if (!target) return current;
      const statusIndex = target.columns.findIndex((column) => ['사용여부', '상태', '결재 상태'].includes(column));
      if (statusIndex < 0) return current;

      const rows = target.rows.map((row, index) =>
        index === rowIndex ? row.map((cell, cellIndex) => (cellIndex === statusIndex ? status : cell)) : row,
      );

      return {
        ...current,
        [listKey]: { ...target, rows },
      };
    });
    writeAuditLog(listKey, 'UPDATE', `${listKey} 상태 변경`);
  };

  const removeRow = async (listKey, rowIndex) => {
    const removedRow = lists[listKey]?.rows[rowIndex];
    if (apiStatus.connected && removedRow?._meta) {
      await deleteBackendRow(listKey, removedRow, user);
      await writeAuditLog(listKey, 'DELETE', `${listKey} 데이터 삭제`, {
        recordId: removedRow._meta[`${listKey.slice(0, -1)}Id`] ?? removedRow._meta.id ?? 0,
        beforeData: removedRow._meta,
        afterData: null,
      });
      await refreshBackendState();
      return;
    }
    setLists((current) => {
      const target = current[listKey];
      if (!target) return current;
      const rows = target.rows.filter((_, index) => index !== rowIndex);

      return {
        ...current,
        [listKey]: { ...target, rows, total: `총 ${rows.length}건` },
      };
    });
    if (listKey === 'users' && removedRow?.[1]) {
      setAccounts((current) => current.filter((account) => account.id !== removedRow[1]));
    }
    writeAuditLog(listKey, 'DELETE', `${listKey} 데이터 삭제`);
  };

  const addMessage = (message) => {
    setSentMessages((current) => [message, ...current]);
    writeAuditLog('messages', 'CREATE', '쪽지 발송');
  };

  const addSchedule = (schedule) => {
    setSchedules((current) => [...current, schedule]);
    writeAuditLog('schedules', 'CREATE', '일정 등록');
  };

  const upsertAccount = (account) => {
    setAccounts((current) => {
      const exists = current.some((item) => item.id === account.id);
      if (exists) {
        return current.map((item) =>
          item.id === account.id ? { ...item, ...account, password: account.password || item.password } : item,
        );
      }
      return [...current, account];
    });
  };

  const saveRoleMenus = (roleCode, rows) => {
    setRoleMenus((current) => ({ ...current, [roleCode]: rows }));
    writeAuditLog('role_menus', 'UPDATE', `${roleCode} 권한별 메뉴 설정`);
  };

  const saveFormRecord = async (formKey, values, options = {}) => {
    if (apiStatus.connected) {
      await saveFormToBackend(formKey, values, {
        user,
        lists,
        resources,
        editingRow: options.editingRow,
        selectedChip: options.selectedChip,
        selectedRoles: options.selectedRoles ?? [],
        files: options.files ?? [],
      });
      const tableName = formAuditTargets[formKey] ?? formKey;
      const actionType = options.editingRow ? 'UPDATE' : 'CREATE';
      await writeAuditLog(tableName, actionType, `${tableName} 데이터 ${options.editingRow ? '수정' : '등록'}`, {
        recordId: getAuditRecordId(formKey, options.editingRow),
        beforeData: options.editingRow?._meta ?? null,
        afterData: { detail: `${tableName} 데이터 ${options.editingRow ? '수정' : '등록'}`, values },
      });
      await refreshBackendState();
      return;
    }
    throw new Error('백엔드에 연결되어 있지 않습니다.');
  };

  const authValue = useMemo(
    () => ({
      user,
      apiStatus,
      login,
      localLogin,
      logout,
      changePassword,
    }),
    [user, apiStatus, accounts],
  );

  const dataValue = useMemo(
    () => ({
      lists,
      schedules,
      messages,
      sentMessages,
      roleMenus,
      resources,
      dashboard,
      permissions,
      permissionsLoaded,
    }),
    [lists, schedules, messages, sentMessages, roleMenus, resources, dashboard, permissions, permissionsLoaded],
  );

  const actionsValue = useMemo(
    () => ({
      addListRow,
      updateListRow,
      updateRowStatus,
      removeRow,
      addSchedule,
      addMessage,
      upsertAccount,
      saveRoleMenus,
      saveFormRecord,
      refreshBackendState,
    }),
    [lists, resources, user, apiStatus],
  );

  return (
    <AuthContext.Provider value={authValue}>
      <GroupwareDataContext.Provider value={dataValue}>
        <GroupwareActionsContext.Provider value={actionsValue}>{children}</GroupwareActionsContext.Provider>
      </GroupwareDataContext.Provider>
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used inside AppProvider');
  return value;
}

export function useGroupwareData() {
  const value = useContext(GroupwareDataContext);
  if (!value) throw new Error('useGroupwareData must be used inside AppProvider');
  return value;
}

export function useGroupwareActions() {
  const value = useContext(GroupwareActionsContext);
  if (!value) throw new Error('useGroupwareActions must be used inside AppProvider');
  return value;
}

export function useApp() {
  return {
    ...useAuth(),
    ...useGroupwareData(),
    ...useGroupwareActions(),
  };
}
