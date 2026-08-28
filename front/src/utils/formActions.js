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
      get('loginId'),
      get('userName'),
      get('department'),
      get('email'),
      '0회',
      get('useYn', '사용'),
      existingRow?.[7] ?? today,
      '보기',
    ],
    roleRegister: () => [
      number,
      get('roleCode'),
      get('roleName'),
      get('roleDescription'),
      '0명',
      get('useYn', '사용'),
      existingRow?.[6] ?? today,
      '수정',
    ],
    menuEdit: () => [
      get('menuName'),
      get('menuUrl'),
      get('sortOrder'),
      '1단계',
      get('useYn', '사용'),
      '수정',
    ],
    noticeRegister: () => [
      number,
      get('importantYn', '일반'),
      get('title'),
      existingRow?.[3] ?? currentUserName,
      `${get('startDate', today)} ~ ${get('endDate', today)}`,
      existingRow?.[5] ?? '0',
      existingRow?.[6] ?? today,
      '수정',
    ],
    boardRegister: () => [
      number,
      get('boardName'),
      get('boardDescription'),
      get('attachmentYn', '허용'),
      get('useYn', '사용'),
      '수정',
    ],
    postRegister: () => [
      number,
      get('boardId'),
      get('title'),
      existingRow?.[3] ?? currentUserName,
      existingRow?.[4] ?? '0',
      existingRow?.[5] ?? today,
      get('useYn', existingRow?.[6] ?? '사용'),
      '상세',
    ],
    reservationRegister: () => [
      number,
      get('reservationType', '회의실'),
      get('resourceName'),
      currentUserName,
      currentUserDepartment,
      get('reservationDate', today),
      `${get('startTime', '09:00')}~${get('endTime', '10:00')}`,
      get('purpose'),
      existingRow?.[8] ?? '대기',
      '상세',
    ],
    taskRegister: () => [
      number,
      get('title'),
      get('assigneeId'),
      existingRow?.[3] ?? currentUserDepartment,
      get('dueDate', today),
      get('taskStatus', existingRow?.[5] ?? '예정'),
      existingRow?.[6] ?? '0%',
      existingRow?.[7] ?? '-',
      existingRow?.[8] ?? '수정',
    ],
    templateRegister: () => [
      number,
      get('templateCode'),
      get('templateName'),
      get('templateDescription'),
      get('useYn', '사용'),
      '수정',
    ],
    codeRegister: () => [
      number,
      get('codeGroupId'),
      get('codeGroupName'),
      `${(values.details ?? []).filter((detail) => detail.codeValue && detail.codeName).length}개`,
      get('description'),
      get('useYn', '사용'),
      '상세',
    ],
  };

  return builders[formKey]?.();
}
