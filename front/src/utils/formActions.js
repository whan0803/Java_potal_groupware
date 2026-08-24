const getToday = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const formTargets = {
  userRegister: { listKey: 'users', redirectTo: '/users' },
  roleRegister: { listKey: 'roles', redirectTo: '/roles' },
  menuEdit: { listKey: 'menus', redirectTo: '/menus' },
  noticeRegister: { listKey: 'notices', redirectTo: '/notices' },
  boardRegister: { listKey: 'boards', redirectTo: '/boards' },
  postRegister: { listKey: 'posts', redirectTo: '/posts' },
  reservationRegister: { listKey: 'reservations', redirectTo: '/reservations' },
  resourceRegister: { redirectTo: '/reservations' },
  templateRegister: { listKey: 'templates', redirectTo: '/templates' },
  taskRegister: { listKey: 'tasks', redirectTo: '/tasks' },
  scheduleRegister: { redirectTo: '/schedule' },
  messageCompose: { redirectTo: '/messages' },
  codeRegister: { listKey: 'codes', redirectTo: '/codes' },
};

export function buildRow(formKey, values, rowCount, existingRow = null, user = null) {
  const today = getToday();
  const number = String(rowCount + 1);
  const get = (label, fallback = '') => values[label]?.trim() || fallback;
  const currentUserName = user?.name ?? '';
  const currentUserDepartment = user?.department ?? user?.deptName ?? '';

  const builders = {
    userRegister: () => [
      number,
      get('아이디'),
      get('이름'),
      get('부서'),
      get('이메일'),
      '0회',
      get('사용여부', '사용'),
      existingRow?.[7] ?? today,
      '보기',
    ],
    roleRegister: () => [
      number,
      get('권한 코드'),
      get('권한명'),
      get('설명'),
      '0명',
      get('사용여부', '사용'),
      existingRow?.[6] ?? today,
      '수정',
    ],
    menuEdit: () => [
      get('메뉴명'),
      get('URL'),
      get('정렬 순서'),
      '1단계',
      get('사용여부', '사용'),
      '수정',
    ],
    noticeRegister: () => [
      number,
      get('중요 공지 여부', '일반'),
      get('제목'),
      existingRow?.[3] ?? currentUserName,
      `${get('게시 시작일', today)} ~ ${get('게시 종료일', today)}`,
      existingRow?.[5] ?? '0',
      existingRow?.[6] ?? today,
      '수정',
    ],
    boardRegister: () => [
      number,
      get('게시판명'),
      get('설명'),
      get('첨부파일 허용 여부', '허용'),
      get('사용여부', '사용'),
      '수정',
    ],
    postRegister: () => [
      number,
      get('게시판 ID'),
      get('제목'),
      existingRow?.[3] ?? currentUserName,
      existingRow?.[4] ?? '0',
      existingRow?.[5] ?? today,
      get('사용여부', existingRow?.[6] ?? '사용'),
      '상세',
    ],
    reservationRegister: () => [
      number,
      get('예약 유형', '회의실'),
      get('자원 선택'),
      currentUserName,
      currentUserDepartment,
      get('예약일', today),
      `${get('시작 시간', '09:00')}~${get('종료 시간', '10:00')}`,
      get('사용 목적'),
      existingRow?.[8] ?? '대기',
      '상세',
    ],
    taskRegister: () => [
      number,
      get('업무 제목'),
      get('담당자'),
      existingRow?.[3] ?? currentUserDepartment,
      get('마감일', today),
      existingRow?.[5] ?? '예정',
      existingRow?.[6] ?? '0%',
      existingRow?.[7] ?? '-',
      existingRow?.[8] ?? '수정',
    ],
    templateRegister: () => [
      number,
      get('양식 코드'),
      get('양식명'),
      get('설명'),
      get('사용여부', '사용'),
      '수정',
    ],
    codeRegister: () => [
      number,
      get('코드 그룹 ID'),
      get('그룹명'),
      get('설명'),
      get('사용여부', '사용'),
      '상세',
    ],
  };

  return builders[formKey]?.();
}
