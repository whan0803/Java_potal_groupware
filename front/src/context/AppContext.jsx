import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { listScreens } from '../data/listScreens.js';
import { authApi } from '../services/api.js';
import {
  deleteBackendRow,
  fetchBackendState,
  saveFormToBackend,
  updateBackendStatus,
} from '../services/backendAdapters.js';
import { loadStorage, saveStorage } from '../utils/storage.js';

const AppContext = createContext(null);

const initialAccounts = [
  { id: 'admin', password: 'admin123', name: '홍길동', role: '시스템 관리자', enabled: true },
  { id: 'user', password: 'user123', name: '김지수', role: '일반 사용자', enabled: true },
  { id: 'disabled', password: 'disabled123', name: '비활성 사용자', role: '일반 사용자', enabled: false },
];

const initialMessages = [
  ['김지수', '07-27 09:15', '7월 인사 발령 안내', '인사팀', '7월 인사 발령 안내 관련 내용입니다.'],
  ['최현우', '07-26 16:30', '서버 점검 협조 요청', 'IT기획팀', '서버 점검 시간 동안 업무 시스템 접속이 제한될 수 있습니다.'],
  ['이민호', '07-25 11:00', '영업 자료 IT 지원 요청', '영업팀', '영업 제안 자료 준비를 위한 IT 지원을 요청합니다.'],
  ['정유나', '07-27 14:20', '재무 시스템 오류 문의', '재무팀', '재무 시스템 오류 확인을 부탁드립니다.'],
];

const initialSentMessages = [
  ['홍길동', '07-26 10:20', '회의 자료 전달드립니다', 'IT기획팀', '회의 자료를 전달드립니다.'],
  ['홍길동', '07-25 15:40', '시스템 점검 확인 요청', 'IT기획팀', '시스템 점검 전 확인을 요청드립니다.'],
];

const initialRoleMenus = {
  ROLE_ADMIN: [
    { name: '사용자 목록', url: '/users/list', read: true, create: true, update: true, delete: true },
    { name: '사용자 등록', url: '/users/new', read: true, create: true, update: true, delete: true },
    { name: '권한 목록', url: '/roles/list', read: true, create: true, update: true, delete: false },
    { name: '권한 등록', url: '/roles/new', read: true, create: true, update: true, delete: true },
    { name: '권한별 메뉴 설정', url: '/roles/menu', read: true, create: true, update: true, delete: true },
    { name: '메뉴 목록', url: '/menus/list', read: true, create: true, update: true, delete: true },
    { name: '공지사항 목록', url: '/notices/list', read: true, create: true, update: true, delete: false },
    { name: '예약 목록', url: '/reservations', read: true, create: true, update: true, delete: false },
    { name: '업무 목록', url: '/tasks', read: true, create: true, update: true, delete: false },
  ],
};

const toUserSession = (response) => ({
  userId: response.userId,
  id: response.loginId,
  name: response.userName,
  role: response.roles?.[0] ?? '사용자',
  roles: response.roles ?? [],
});

export function AppProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accounts, setAccounts] = useState(() => loadStorage('accounts', initialAccounts));
  const [lists, setLists] = useState(() => loadStorage('lists', listScreens));
  const [schedules, setSchedules] = useState(() => loadStorage('schedules', []));
  const [messages, setMessages] = useState(() => loadStorage('messages', initialMessages));
  const [sentMessages, setSentMessages] = useState(() => loadStorage('sentMessages', initialSentMessages));
  const [roleMenus, setRoleMenus] = useState(() => loadStorage('roleMenus', initialRoleMenus));
  const [resources, setResources] = useState(() => loadStorage('resources', []));
  const [dashboard, setDashboard] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [apiStatus, setApiStatus] = useState({ connected: false, loading: false, error: '' });

  useEffect(() => {
    window.localStorage.removeItem('groupware-admin:user');
  }, []);
  useEffect(() => saveStorage('accounts', accounts), [accounts]);
  useEffect(() => saveStorage('lists', lists), [lists]);
  useEffect(() => saveStorage('schedules', schedules), [schedules]);
  useEffect(() => saveStorage('messages', messages), [messages]);
  useEffect(() => saveStorage('sentMessages', sentMessages), [sentMessages]);
  useEffect(() => saveStorage('roleMenus', roleMenus), [roleMenus]);
  useEffect(() => saveStorage('resources', resources), [resources]);

  const refreshBackendState = async () => {
    if (!user) return;
    setApiStatus((current) => ({ ...current, loading: true, error: '' }));
    try {
      const session = await authApi.me();
      const sessionUser = toUserSession(session);
      setUser(sessionUser);
      const state = await fetchBackendState(sessionUser);
      setLists((current) => ({ ...current, ...state.lists, logs: current.logs }));
      setSchedules(state.schedules);
      setMessages(state.messages);
      setSentMessages(state.sentMessages);
      setResources(state.resources);
      setDashboard(state.dashboard);
      setPermissions(state.permissions);
      setApiStatus({ connected: true, loading: false, error: '' });
    } catch (error) {
      setApiStatus({ connected: false, loading: false, error: error.message });
    }
  };

  useEffect(() => {
    refreshBackendState();
  }, [user?.userId, user?.id]);

  const writeAuditLog = (tableName, actionType, detail) => {
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

  const login = async ({ id, password }) => {
    try {
      const response = await authApi.login({ id, password });
      const session = await authApi.me();
      setUser(toUserSession(session));
      setApiStatus({ connected: true, loading: false, error: '' });
      return { ok: true };
    } catch (error) {
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
      setDashboard(null);
      setPermissions([]);
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
      await refreshBackendState();
      return;
    }
    throw new Error('백엔드에 연결되어 있지 않습니다.');
  };

  const value = useMemo(
    () => ({
      user,
      lists,
      schedules,
      messages,
      sentMessages,
      roleMenus,
      resources,
      dashboard,
      permissions,
      apiStatus,
      login,
      localLogin,
      logout,
      changePassword,
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
    [user, accounts, lists, schedules, messages, sentMessages, roleMenus, resources, dashboard, permissions, apiStatus],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const value = useContext(AppContext);
  if (!value) throw new Error('useApp must be used inside AppProvider');
  return value;
}
