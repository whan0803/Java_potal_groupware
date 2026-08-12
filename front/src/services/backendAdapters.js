import { api, attachmentApi, getPageItems, listApi } from './api.js';

const formatDate = (value) => String(value ?? '').slice(0, 10);
const formatTime = (value) => String(value ?? '').slice(11, 16);
const ynLabel = (value) => (value === 'Y' ? '사용' : '미사용');
const ynValue = (value, yesLabel = '사용') => (value === yesLabel || value === 'Y' || value === '예' ? 'Y' : 'N');
const importantLabel = (value) => (value === 'Y' ? '중요' : '일반');
const attachmentLabel = (value) => (value === 'Y' ? '허용' : '미허용');
const statusLabel = (value) => {
  const labels = {
    REQUESTED: '대기',
    APPROVED: '승인',
    REJECTED: '반려',
    CANCELLED: '취소',
    CANCELED: '취소',
    PENDING: '진행중',
    IN_PROGRESS: '진행중',
    READY: '예정',
    DONE: '완료',
    COMPLETED: '완료',
    HOLD: '보류',
    DRAFT: '임시저장',
  };
  return labels[value] ?? value ?? '-';
};
const approvalStatusLabel = (value) => {
  const labels = {
    DRAFT: '임시저장',
    IN_PROGRESS: '진행중',
    APPROVED: '완료',
    REJECTED: '반려',
    CANCELED: '취소',
  };
  return labels[value] ?? statusLabel(value);
};
const statusValue = (value) => {
  const values = {
    대기: 'REQUESTED',
    승인: 'APPROVED',
    반려: 'REJECTED',
    취소: 'CANCELLED',
    진행중: 'IN_PROGRESS',
    완료: 'COMPLETED',
    예정: 'READY',
    보류: 'HOLD',
  };
  return values[value] ?? value;
};

const withMeta = (row, meta) => Object.assign(row, { _meta: meta });
const page = (response) => getPageItems(response);
const loadUserDepartments = () => {
  try {
    return JSON.parse(window.localStorage.getItem('groupware-admin:userDepartments') ?? '{}');
  } catch {
    return {};
  }
};
const saveUserDepartment = (userId, loginId, department) => {
  if (!department) return;
  try {
    const departments = loadUserDepartments();
    const key = String(userId ?? loginId);
    departments[key] = department;
    if (loginId) departments[String(loginId)] = department;
    window.localStorage.setItem('groupware-admin:userDepartments', JSON.stringify(departments));
  } catch {
    // Department is optional display data; failed storage should not block saving.
  }
};
const formListKeys = {
  userRegister: 'users',
  roleRegister: 'roles',
  menuEdit: 'menus',
  noticeRegister: 'notices',
  boardRegister: 'boards',
  postRegister: 'posts',
  reservationRegister: 'reservations',
  templateRegister: 'templates',
  taskRegister: 'tasks',
  codeRegister: 'codes',
};
const formIdKeys = {
  userRegister: 'userId',
  roleRegister: 'roleId',
  menuEdit: 'menuId',
  noticeRegister: 'noticeId',
  boardRegister: 'boardId',
  postRegister: 'postId',
  reservationRegister: 'reservationId',
  templateRegister: 'templateId',
  taskRegister: 'taskId',
  codeRegister: 'codeGroupId',
};
const attachmentReferenceTypes = {
  noticeRegister: 'NOTICE',
  postRegister: 'POST',
  taskRegister: 'TASK',
};

const flattenMenus = (menus, rows = []) => {
  menus.forEach((menu) => {
    rows.push(menu);
    if (menu.children?.length) flattenMenus(menu.children, rows);
  });
  return rows;
};

export function getCurrentUserId(user, fallback = 1) {
  return Number(user?.userId ?? user?.user_id ?? user?.id) || fallback;
}

function resolveEditingMeta(formKey, values, lists, editingRow) {
  if (!editingRow) return {};
  if (editingRow._meta) return editingRow._meta;

  const listKey = formListKeys[formKey];
  const rows = lists[listKey]?.rows ?? [];
  const matchers = {
    userRegister: (row) => row[1] === values.아이디,
    roleRegister: (row) => row[1] === values['권한 코드'],
    menuEdit: (row) => row[0] === values.메뉴명 && row[1] === (values.URL || ''),
    noticeRegister: (row) => row[2] === values.제목,
    boardRegister: (row) => row[1] === values.게시판명,
    postRegister: (row) => row[2] === values.제목,
    reservationRegister: (row) =>
      row[2] === values['자원 선택'] &&
      row[5] === values.예약일 &&
      String(row[6] ?? '') === `${values['시작 시간']}~${values['종료 시간']}`,
    templateRegister: (row) => row[1] === values['양식 코드'],
    taskRegister: (row) => row[1] === values['업무 제목'],
    codeRegister: (row) => row[1] === values['코드 그룹 ID'],
  };
  return rows.find((row) => row._meta && (row === editingRow || matchers[formKey]?.(row)))?._meta ?? {};
}

function assertEditableTarget(formKey, editingRow, meta) {
  const idKey = formIdKeys[formKey];
  if (!editingRow || !idKey || meta?.[idKey]) return;
  throw new Error('수정할 대상 ID를 찾을 수 없습니다. 목록을 새로고침한 뒤 다시 수정해 주세요.');
}

export async function fetchBackendState(user) {
  const userId = getCurrentUserId(user);
  const now = new Date();
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 19);
  const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59).toISOString().slice(0, 19);

  const settled = await Promise.allSettled([
    listApi.users(),
    listApi.roles(),
    listApi.menus(),
    listApi.notices(),
    listApi.boards(),
    listApi.posts(),
    listApi.resources('MEETING_ROOM'),
    listApi.resources('VEHICLE'),
    listApi.approvals(),
    listApi.templates(),
    listApi.tasks(),
    listApi.schedules(monthStart, monthEnd),
    listApi.receivedMessages(userId),
    listApi.sentMessages(userId),
    listApi.codes(),
    listApi.dashboard(),
  ]);

  const value = (index, fallback) => (settled[index].status === 'fulfilled' ? settled[index].value : fallback);
  const roles = value(1, []);
  const primaryRole = roles.find((role) => user.roles?.includes(role.roleCode) || user.role === role.roleCode);
  const permissions = primaryRole?.roleId
    ? await listApi.roleMenus(primaryRole.roleId).catch(() => [])
    : [];
  const resources = [...value(6, []), ...value(7, [])];
  const reservations = (
    await Promise.allSettled(resources.map((resource) => listApi.reservations(resource.resourceId)))
  ).flatMap((result) => (result.status === 'fulfilled' ? result.value : []));

  return {
    lists: {
      users: mapUsers(value(0, { content: [] })),
      roles: mapRoles(roles),
      menus: mapMenus(value(2, [])),
      notices: mapNotices(value(3, { content: [] })),
      boards: mapBoards(value(4, [])),
      posts: mapPosts(value(5, { content: [] })),
      reservations: mapReservations(reservations, resources),
      approval: mapApprovals(value(8, [])),
      templates: mapTemplates(value(9, [])),
      tasks: mapTasks(value(10, { content: [] })),
      codes: mapCodes(value(14, [])),
    },
    resources,
    schedules: value(11, []),
    messages: mapReceivedMessages(value(12, { content: [] })),
    sentMessages: mapSentMessages(value(13, { content: [] })),
    dashboard: value(15, null),
    permissions,
  };
}

function mapUsers(response) {
  const departments = loadUserDepartments();
  const rows = page(response).map((item, index) =>
    withMeta([
      String(index + 1),
      item.loginId,
      item.userName,
      item.department ?? departments[String(item.userId)] ?? departments[String(item.loginId)] ?? '',
      item.roles?.map((role) => role.roleName).join(', ') || '-',
      item.email ?? '',
      item.phone ?? '',
      ynLabel(item.useYn),
      formatDate(item.createdAt),
      '보기',
    ], { ...item, department: item.department ?? departments[String(item.userId)] ?? departments[String(item.loginId)] ?? '' }),
  );
  return withRows('이름·아이디·부서·권한 검색', ['No', '아이디', '이름', '부서', '권한', '이메일', '연락처', '사용여부', '등록일', '관리'], rows);
}

function mapRoles(items) {
  const rows = items.map((item, index) =>
    withMeta([
      String(index + 1),
      item.roleCode,
      item.roleName,
      item.roleDescription ?? '',
      '-',
      ynLabel(item.useYn),
      formatDate(item.createdAt),
      '수정',
    ], item),
  );
  return withRows('권한코드·권한명 검색', ['No', '권한 코드', '권한명', '설명', '사용자 수', '사용여부', '등록일', '관리'], rows);
}

function mapMenus(items) {
  const rows = flattenMenus(items).map((item) =>
    withMeta([
      item.menuName,
      item.menuUrl ?? '',
      String(item.sortOrder ?? 0),
      `${item.menuLevel ?? 1}단계`,
      ynLabel(item.useYn),
      '수정',
    ], item),
  );
  return withRows('메뉴명 검색', ['메뉴명', 'URL', '순서', '레벨', '사용여부', '관리'], rows);
}

function mapNotices(response) {
  const rows = page(response).map((item, index) =>
    withMeta([
      String(index + 1),
      importantLabel(item.importantYn),
      item.title,
      item.writerName ?? '',
      `${formatDate(item.startDate)} ~ ${formatDate(item.endDate)}`,
      String(item.viewCount ?? 0),
      formatDate(item.createdAt),
      '수정',
    ], item),
  );
  return withRows('제목 검색', ['No', '구분', '제목', '작성자', '게시기간', '조회수', '등록일', '관리'], rows);
}

function mapBoards(items) {
  const rows = items.map((item, index) =>
    withMeta([
      String(index + 1),
      item.boardName,
      item.boardDescription ?? '',
      attachmentLabel(item.attachmentYn),
      ynLabel(item.useYn),
      '수정',
    ], item),
  );
  return withRows('게시판명 검색', ['No', '게시판명', '설명', '첨부파일', '사용여부', '관리'], rows);
}

function mapPosts(response) {
  const rows = page(response).map((item, index) =>
    withMeta([
      String(index + 1),
      item.boardName ?? item.boardId,
      item.title,
      item.writerName ?? '',
      String(item.viewCount ?? item.viewCoun ?? 0),
      formatDate(item.createdAt),
      ynLabel(item.useYn ?? 'Y'),
      '상세',
    ], item),
  );
  return withRows('제목·내용·작성자 검색', ['No', '게시판', '제목', '작성자', '조회수', '등록일', '사용여부', '관리'], rows);
}

function mapReservations(items, resources) {
  const rows = items.map((item, index) => {
    const resource = resources.find((target) => target.resourceId === item.resourceId);
    return withMeta([
      String(index + 1),
      resource?.resourceType === 'VEHICLE' ? '차량' : '회의실',
      item.resourceName,
      item.requesterName ?? '',
      '-',
      formatDate(item.startDatetime),
      `${formatTime(item.startDatetime)}~${formatTime(item.endDatetime)}`,
      item.purpose ?? item.title ?? '',
      statusLabel(item.status),
      '상세',
    ], { ...item, resource });
  });
  return withRows('자원명·신청자·목적 검색', ['No', '유형', '자원명', '신청자', '부서', '예약일', '시간', '목적', '상태', '관리'], rows);
}

function mapApprovals(items) {
  const rows = page(items).map((item, index) =>
    {
      const approvalStatus = approvalStatusLabel(item.approvalStatus ?? item.status);
      return withMeta([
        String(index + 1),
        item.templateName ?? item.documentType ?? '-',
        item.title,
        item.drafterName ?? '',
        item.departmentName ?? '-',
        formatDate(item.createdAt ?? item.createAt),
        approvalStatus,
        approvalStatus === '진행중' ? '결재 처리' : '처리 완료',
      ], item);
    }
  );
  return { tabs: ['결재 대기', '결재 완료'], total: `총 ${rows.length}건`, columns: ['No', '문서 유형', '제목', '기안자', '부서', '기안일', '결재 상태', '처리'], rows };
}

function mapTemplates(items) {
  const rows = page(items).map((item, index) =>
    withMeta([
      String(index + 1),
      item.templateCode,
      item.templateName,
      item.templateDescription ?? '',
      ynLabel(item.useYn),
      '상세',
    ], item),
  );
  return withRows('양식 코드·양식명 검색', ['No', '양식 코드', '양식명', '설명', '사용여부', '관리'], rows);
}

function mapTasks(response) {
  const rows = page(response).map((item, index) =>
    withMeta([
      String(index + 1),
      item.title,
      item.assigneeName ?? '',
      '-',
      formatDate(item.dueDate),
      statusLabel(item.taskStatus),
      `${item.progressRate ?? 0}%`,
      item.progressRate >= 100 ? formatDate(item.dueDate) : '-',
      '수정',
    ], item),
  );
  return withRows('업무명·담당자 검색', ['No', '업무명', '담당자', '부서', '마감일', '상태', '진행률', '완료일', '관리'], rows);
}

function mapCodes(items) {
  const rows = items.map((item, index) =>
    withMeta([
      String(index + 1),
      item.codeGroupId,
      item.codeGroupName,
      item.description ?? '',
      ynLabel(item.useYn),
      '상세',
    ], item),
  );
  return withRows('코드 그룹명 검색', ['No', '코드 그룹 ID', '그룹명', '설명', '사용여부', '관리'], rows);
}

function mapReceivedMessages(response) {
  return page(response).map((item) =>
    withMeta([item.senderName, formatDate(item.receivedAt), item.title, item.readYn === 'Y' ? '읽음' : '미확인', ''], item),
  );
}

function mapSentMessages(response) {
  return page(response).map((item) =>
    withMeta(['나', formatDate(item.sentAt), item.title, item.receivedName, ''], item),
  );
}

function withRows(search, columns, rows) {
  return { search, total: `총 ${rows.length}건`, columns, rows };
}

export async function saveFormToBackend(formKey, values, context) {
  const { user, lists, resources, editingRow, selectedChip, files = [] } = context;
  const userId = getCurrentUserId(user);
  const meta = resolveEditingMeta(formKey, values, lists, editingRow);
  assertEditableTarget(formKey, editingRow, meta);
  const roleIdByName = (name) => lists.roles.rows.find((row) => row[2] === name || row[1] === name)?._meta?.roleId;
  const boardId = Number(values['게시판 ID']) || lists.boards.rows.find((row) => row[1] === values['게시판 ID'])?._meta?.boardId || lists.boards.rows[0]?._meta?.boardId;
  const assigneeId = Number(values.담당자) || lists.users.rows.find((row) => row[2] === values.담당자 || row[1] === values.담당자)?._meta?.userId || userId;
  const selectedResourceType = selectedChip === '차량' ? 'VEHICLE' : 'MEETING_ROOM';
  const resource = resources.find((item) => item.resourceName === values['자원 선택'] && item.resourceType === selectedResourceType)
    ?? resources.find((item) => item.resourceName === values['자원 선택'])
    ?? resources.find((item) => item.resourceType === selectedResourceType);

  const handlers = {
    userRegister: () => {
      saveUserDepartment(meta.userId, values.아이디, values.부서);
      const body = {
        loginId: values.아이디,
        password: values.비밀번호 || (meta.userId ? undefined : 'user12345'),
        userName: values.이름,
        email: values.이메일 || null,
        phone: values.연락처 || null,
        useYn: ynValue(values.사용여부),
        roleIds: context.selectedRoles.map(roleIdByName).filter(Boolean),
      };
      return meta.userId ? api.put(`/api/users/${meta.userId}`, body) : api.post('/api/users', body);
    },
    roleRegister: () => {
      const body = {
        roleCode: values['권한 코드'],
        roleName: values.권한명,
        roleDescription: values.설명 || '',
        useYn: ynValue(values.사용여부),
        createdBy: userId,
        updatedBy: userId,
      };
      return meta.roleId ? api.put(`/api/roles/${meta.roleId}`, body) : api.post('/api/roles', body);
    },
    menuEdit: () => {
      const body = {
        menuName: values.메뉴명,
        menuUrl: values.URL || null,
        parentMenuId: null,
        sortOrder: Number(values['정렬 순서']) || 0,
        useYn: ynValue(values.사용여부),
        userId,
      };
      return meta.menuId ? api.put(`/api/menu/${meta.menuId}`, body) : api.post('/api/menu', body);
    },
    noticeRegister: () => {
      const body = {
        title: values.제목,
        content: values.내용,
        writerId: userId,
        startDate: values['게시 시작일'],
        endDate: values['게시 종료일'],
        importantYn: ynValue(values['중요 공지 여부'], '중요'),
        useYn: values.사용여부 ? ynValue(values.사용여부) : meta.useYn ?? 'Y',
        userId,
        admin: true,
      };
      return meta.noticeId ? api.put(`/api/notices/${meta.noticeId}`, body) : api.post('/api/notices', body);
    },
    boardRegister: () => {
      const body = {
        boardName: values.게시판명,
        boardDescription: values.설명 || '',
        attachmentYn: ynValue(values['첨부파일 허용 여부'], '허용'),
        useYn: ynValue(values.사용여부),
        userId,
      };
      return meta.boardId ? api.put(`/api/boards/${meta.boardId}`, body) : api.post('/api/boards', body);
    },
    postRegister: async () => {
      const body = { boardId, title: values.제목, content: values.내용 || values.제목, writerId: userId };
      const postId = meta.postId
        ? (await api.put(`/api/posts/${meta.postId}`, { ...body, userId, admin: isAdminUser(user) }), meta.postId)
        : await api.post('/api/posts', body);
      await uploadFiles(formKey, postId, files, userId);
      return postId;
    },
    reservationRegister: () => {
      const body = {
        resourceId: resource?.resourceId,
        resourceName: values['자원 선택'],
        resourceType: selectedResourceType,
        requesterId: userId,
        title: values['사용 목적'] || '예약 신청',
        purpose: values['사용 목적'] || '',
        startDateTime: `${values.예약일}T${values['시작 시간']}:00`,
        endDateTime: `${values.예약일}T${values['종료 시간']}:00`,
      };
      return meta.reservationId ? api.put(`/api/reservations/${meta.reservationId}`, body) : api.post('/api/reservations', body);
    },
    resourceRegister: () => api.post('/api/reservations/resources', {
      resourceType: values['자원 유형'] === '차량' ? 'VEHICLE' : 'MEETING_ROOM',
      resourceName: values.자원명,
      resourceDescription: values.설명 || '',
      capacity: Number(values['수용/탑승 인원']) || null,
      location: values.위치 || '',
      vehicleNumber: values['차량 번호'] || '',
    }),
    templateRegister: () => {
      const body = {
        templateCode: values['양식 코드'],
        templateName: values.양식명,
        templateDescription: values.설명 || '',
        templateContent: values['기본 내용'] || values.양식명,
        useYn: ynValue(values.사용여부),
        createdBy: userId,
        updatedBy: userId,
      };
      return meta.templateId ? api.put(`/api/document-templates/${meta.templateId}`, body) : api.post('/api/document-templates', body);
    },
    taskRegister: () => {
      const body = {
        requesterId: userId,
        assigneeId,
        title: values['업무 제목'],
        content: values['업무 내용'] || '',
        taskStatus: statusValue(values.상태 || meta.taskStatus || '예정'),
        priority: values.우선순위 || meta.priority || 'NORMAL',
        startDate: values.시작일 || formatDate(new Date().toISOString()),
        dueDate: values.마감일,
      };
      return meta.taskId ? api.put(`/api/tasks/${meta.taskId}`, body) : api.post('/api/tasks', body);
    },
    scheduleRegister: () => {
      const allDayYn = ynValue(values['종일 일정 여부'], '예');
      const body = {
        title: values.제목,
        content: values.내용 || '',
        location: values.장소 || '',
        scheduleType: values['일정 유형'] || 'PERSONAL',
        startDatetime: `${values.시작일}T${allDayYn === 'Y' ? '00:00' : values['시작 시간'] || '09:00'}:00`,
        endDatetime: `${values.종료일}T${allDayYn === 'Y' ? '23:59' : values['종료 시간'] || '10:00'}:00`,
        allDayYn,
      };
      return meta.scheduleId ? api.put(`/api/schedules/${meta.scheduleId}`, body) : api.post('/api/schedules', body);
    },
    messageCompose: () => {
      const receiveId = Number(values.수신자) || lists.users.rows.find((row) => row[2] === values.수신자 || row[1] === values.수신자)?._meta?.userId;
      return api.post(`/api/messages?senderId=${userId}`, { receiveId, title: values.제목, content: values.내용 });
    },
    codeRegister: () => {
      const body = {
        codeGroupId: values['코드 그룹 ID'],
        codeGroupName: values.그룹명,
        description: values.설명 || '',
        useYn: ynValue(values.사용여부),
        createdBy: userId,
        updatedBy: userId,
      };
      return meta.codeGroupId ? api.put(`/api/common-codes/${meta.codeGroupId}`, body) : api.post('/api/common-codes', body);
    },
  };

  const result = await handlers[formKey]?.();
  const referenceType = attachmentReferenceTypes[formKey];
  const referenceId = getReferenceId(formKey, meta, result);
  if (referenceType && formKey !== 'postRegister') {
    await uploadFiles(formKey, referenceId, files, userId);
  }
  return result;
}

function getReferenceId(formKey, meta, result) {
  const idKey = formIdKeys[formKey];
  if (meta?.[idKey]) return meta[idKey];
  if (typeof result === 'number') return result;
  return result?.[idKey] ?? result?.id;
}

async function uploadFiles(formKey, referenceId, files, userId) {
  const referenceType = attachmentReferenceTypes[formKey];
  if (!referenceType || !referenceId || !files?.length) return;
  await Promise.all(files.map((file) => attachmentApi.upload({ file, referenceType, referenceId, userId })));
}

function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export async function deleteBackendRow(listKey, row, user) {
  const meta = row?._meta ?? {};
  const userId = getCurrentUserId(user);
  const handlers = {
    users: () => api.patch(`/api/users/${meta.userId}/deactivate`),
    roles: () => api.delete(`/api/roles/${meta.roleId}`),
    menus: () => api.delete(`/api/menu/${meta.menuId}`),
    notices: () => api.patch(`/api/notices/${meta.noticeId}/delete`, { userId, admin: isAdminUser(user) }),
    boards: () => api.patch(`/api/boards/${meta.boardId}/disable`, { userId }),
    posts: () => api.patch(`/api/posts/${meta.postId}/delete`, { userId, admin: isAdminUser(user) }),
    reservations: () => api.patch(`/api/reservations/${meta.reservationId}/cancel`),
    templates: () => api.delete(`/api/document-templates/${meta.templateId}`),
    tasks: () => api.delete(`/api/tasks/${meta.taskId}`),
  };
  return handlers[listKey]?.();
}

export async function updateBackendStatus(listKey, row, status, user) {
  const meta = row?._meta ?? {};
  const userId = getCurrentUserId(user);
  if (listKey === 'tasks') return api.patch(`/api/tasks/${meta.taskId}/status`, { taskStatus: statusValue(status) });
  if (listKey === 'approval' && status === '완료') return api.patch(`/api/approvals/${meta.approvalDocumentId ?? meta.approvalId}/approve`, { approverId: userId, comment: '' });
  if (listKey === 'reservations' && ['승인', '반려'].includes(status)) {
    return api.patch(`/api/reservations/${meta.reservationId}/status`, {
      status: status === '승인' ? 'APPROVED' : 'REJECTED',
      approverId: userId,
      approvalComment: '',
    });
  }
  if (listKey === 'reservations' && status === '취소') return api.patch(`/api/reservations/${meta.reservationId}/cancel`);
  return null;
}
