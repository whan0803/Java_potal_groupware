import { createContext, useContext, useMemo, useState } from 'react';
import { listScreens } from '../data/listScreens.js';

const AppContext = createContext(null);

const accounts = [
  { id: 'admin', password: 'admin123', name: '홍길동', role: '시스템 관리자', enabled: true },
  { id: 'user', password: 'user123', name: '김지수', role: '일반 사용자', enabled: true },
  { id: 'disabled', password: 'disabled123', name: '비활성 사용자', role: '일반 사용자', enabled: false },
];

export function AppProvider({ children }) {
  const [user, setUser] = useState(null);
  const [lists, setLists] = useState(listScreens);
  const [schedules, setSchedules] = useState([]);
  const [messages, setMessages] = useState([
    ['김지수', '07-27 09:15', '7월 인사 발령 안내', '인사팀'],
    ['최현우', '07-26 16:30', '서버 점검 협조 요청', 'IT기획팀'],
    ['이민호', '07-25 11:00', '영업 자료 IT 지원 요청', '영업팀'],
    ['정유나', '07-27 14:20', '재무 시스템 오류 문의', '재무팀'],
  ]);
  const [sentMessages, setSentMessages] = useState([
    ['홍길동', '07-26 10:20', '회의 자료 전달드립니다', 'IT기획팀'],
    ['홍길동', '07-25 15:40', '시스템 점검 확인 요청', 'IT기획팀'],
  ]);

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

  const login = ({ id, password }) => {
    const account = accounts.find((item) => item.id === id);
    if (!account) return { ok: false, message: '존재하지 않는 사용자입니다.' };
    if (account.password !== password) return { ok: false, message: '비밀번호가 일치하지 않습니다.' };
    if (!account.enabled) return { ok: false, message: '사용 중지된 계정입니다.' };

    setUser({ id: account.id, name: account.name, role: account.role });
    return { ok: true };
  };

  const logout = () => {
    setUser(null);
  };

  const changePassword = ({ currentPassword, nextPassword, confirmPassword }) => {
    if (!currentPassword || !nextPassword || !confirmPassword) {
      return { ok: false, message: '모든 항목을 입력하세요.' };
    }
    const account = accounts.find((item) => item.id === user?.id);
    if (account?.password !== currentPassword) {
      return { ok: false, message: '기존 비밀번호가 일치하지 않습니다.' };
    }
    if (nextPassword !== confirmPassword) {
      return { ok: false, message: '새 비밀번호와 확인 값이 다릅니다.' };
    }
    if (nextPassword.length < 6) {
      return { ok: false, message: '새 비밀번호는 6자 이상이어야 합니다.' };
    }
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

  const updateRowStatus = (listKey, rowIndex, status = '미사용') => {
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

  const removeRow = (listKey, rowIndex) => {
    setLists((current) => {
      const target = current[listKey];
      if (!target) return current;
      const rows = target.rows.filter((_, index) => index !== rowIndex);

      return {
        ...current,
        [listKey]: { ...target, rows, total: `총 ${rows.length}건` },
      };
    });
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

  const value = useMemo(
    () => ({
      user,
      lists,
      schedules,
      messages,
      sentMessages,
      login,
      logout,
      changePassword,
      addListRow,
      updateRowStatus,
      removeRow,
      addSchedule,
      addMessage,
    }),
    [user, lists, schedules, messages, sentMessages],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const value = useContext(AppContext);
  if (!value) throw new Error('useApp must be used inside AppProvider');
  return value;
}
