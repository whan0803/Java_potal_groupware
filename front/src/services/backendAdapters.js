import { api, attachmentApi, getPageItems, listApi } from './api.js';

const formatDate = (value) => String(value ?? '').slice(0, 10);
const formatTime = (value) => String(value ?? '').slice(11, 16);
const ynLabel = (value) => (value === 'Y' ? '사용' : '미사용');
const ynValue = (value, yesLabel = '사용') => (value === yesLabel || value === 'Y' || value === '예' ? 'Y' : 'N');
const permissionValue = (value) => (value === true || String(value ?? '').trim().toUpperCase() === 'Y' ? 'Y' : 'N');
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
const normalizeRoleCode = (roleCode) => {
  const normalized = String(roleCode ?? '').trim().toUpperCase();
  if (!normalized) return '';
  return normalized.startsWith('ROLE_') ? normalized : `ROLE_${normalized}`;
};
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
    userRegister: (row) => row[1] === values.loginId,
    roleRegister: (row) => row[1] === values.roleCode,
    menuEdit: (row) => row[0] === values.menuName && row[1] === (values.menuUrl || ''),
    noticeRegister: (row) => row[2] === values.title,
    boardRegister: (row) => row[1] === values.boardName,
    postRegister: (row) => row[2] === values.title,
    reservationRegister: (row) =>
      row[2] === values.resourceName &&
      row[5] === values.reservationDate &&
      String(row[6] ?? '') === `${values.startTime}~${values.endTime}`,
    templateRegister: (row) => row[1] === values.templateCode,
    taskRegister: (row) => row[1] === values.title,
    codeRegister: (row) => row[1] === values.codeGroupId,
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
    listApi.auditLogs(),
  ]);

  const value = (index, fallback) => (settled[index].status === 'fulfilled' ? settled[index].value : fallback);
  const roles = value(1, []);
  const permissions = mergeRoleMenuPermissions(await listApi.myRoleMenus());
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
      logs: mapAuditLogs(value(16, { content: [] })),
    },
    resources,
    schedules: value(11, []),
    messages: mapReceivedMessages(value(12, { content: [] })),
    sentMessages: mapSentMessages(value(13, { content: [] })),
    dashboard: value(15, null),
    permissions,
  };
}

function mergeRoleMenuPermissions(items) {
  const merged = new Map();
  items.forEach((item) => {
    const key = item.menuId ?? item.menuUrl ?? item.menuName;
    if (!key) return;
    const current = merged.get(key);
    if (!current) {
      merged.set(key, { ...item });
      return;
    }
    merged.set(key, {
      ...current,
      readYn: permissionValue(current.readYn) === 'Y' || permissionValue(item.readYn) === 'Y' ? 'Y' : 'N',
      createYn: permissionValue(current.createYn) === 'Y' || permissionValue(item.createYn) === 'Y' ? 'Y' : 'N',
      updateYn: permissionValue(current.updateYn) === 'Y' || permissionValue(item.updateYn) === 'Y' ? 'Y' : 'N',
      deleteYn: permissionValue(current.deleteYn) === 'Y' || permissionValue(item.deleteYn) === 'Y' ? 'Y' : 'N',
    });
  });
  return [...merged.values()];
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
      item.statusName ?? statusLabel(item.status),
      '상세',
    ], { ...item, resource });
  });
  return withRows('자원명·신청자·목적 검색', ['No', '유형', '자원명', '신청자', '부서', '예약일', '시간', '목적', '상태', '관리'], rows);
}

function mapApprovals(items) {
  const rows = page(items).map((item, index) =>
    {
      const approvalStatus = item.approvalStatusName ?? approvalStatusLabel(item.approvalStatus ?? item.status);
      const rawApprovalStatus = item.approvalStatus ?? item.status;
      return withMeta([
        String(index + 1),
        item.templateName ?? item.documentType ?? '-',
        item.title,
        item.drafterName ?? '',
        item.departmentName ?? '-',
        formatDate(item.createdAt ?? item.createAt),
        approvalStatus,
        rawApprovalStatus === 'IN_PROGRESS' ? '결재 처리' : '처리 완료',
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
      `${item.detailCount ?? item.details?.length ?? 0}개`,
      item.description ?? '',
      ynLabel(item.useYn),
      '상세',
    ], item),
  );
  return withRows('코드 그룹명 검색', ['No', '코드 그룹 ID', '그룹명', '상세코드', '설명', '사용여부', '관리'], rows);
}

function mapAuditLogs(response) {
  const rows = page(response).map((item, index) =>
    [
      String(index + 1),
      item.tableName,
      item.actionType,
      String(item.actorId ?? '-'),
      formatDate(item.createdAt),
      auditLogDetail(item),
      '상세',
    ],
  );
  return withRows('테이블명·작업자 검색', ['No', '테이블명', '작업 유형', '작업자', '작업 시간', '변경 내용', '관리'], rows);
}

function auditLogDetail(item) {
  if (item.recordId && item.recordId !== 0) return `${item.tableName}#${item.recordId}`;
  return `${item.tableName} ${item.actionType}`;
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
  const boardId = Number(values.boardId) || lists.boards.rows.find((row) => row[1] === values.boardId)?._meta?.boardId || lists.boards.rows[0]?._meta?.boardId;
  const assigneeId = Number(values.assigneeId) || lists.users.rows.find((row) => row[2] === values.assigneeId || row[1] === values.assigneeId)?._meta?.userId || userId;
  const selectedResourceType = selectedChip === '차량' ? 'VEHICLE' : 'MEETING_ROOM';
  const resource = resources.find((item) => item.resourceName === values.resourceName && item.resourceType === selectedResourceType)
    ?? resources.find((item) => item.resourceName === values.resourceName)
    ?? resources.find((item) => item.resourceType === selectedResourceType);

  const handlers = {
    userRegister: () => {
      saveUserDepartment(meta.userId, values.loginId, values.department);
      const body = {
        loginId: values.loginId,
        password: values.password || undefined,
        userName: values.userName,
        email: values.email || null,
        phone: values.phone || null,
        useYn: ynValue(values.useYn),
        roleIds: context.selectedRoles.map(roleIdByName).filter(Boolean),
      };
      return meta.userId ? api.put(`/api/users/${meta.userId}`, body) : api.post('/api/users', body);
    },
    roleRegister: () => {
      const body = {
        roleCode: values.roleCode,
        roleName: values.roleName,
        roleDescription: values.roleDescription || '',
        useYn: ynValue(values.useYn),
        createdBy: userId,
        updatedBy: userId,
      };
      return meta.roleId ? api.put(`/api/roles/${meta.roleId}`, body) : api.post('/api/roles', body);
    },
    menuEdit: () => {
      const body = {
        menuName: values.menuName,
        menuUrl: values.menuUrl || null,
        parentMenuId: null,
        sortOrder: Number(values.sortOrder) || 0,
        useYn: ynValue(values.useYn),
        userId,
      };
      return meta.menuId ? api.put(`/api/menu/${meta.menuId}`, body) : api.post('/api/menu', body);
    },
    noticeRegister: () => {
      const body = {
        title: values.title,
        content: values.content,
        writerId: userId,
        startDate: values.startDate,
        endDate: values.endDate,
        importantYn: ynValue(values.importantYn, '중요'),
        useYn: values.useYn ? ynValue(values.useYn) : meta.useYn ?? 'Y',
        userId,
        admin: true,
      };
      return meta.noticeId ? api.put(`/api/notices/${meta.noticeId}`, body) : api.post('/api/notices', body);
    },
    boardRegister: () => {
      const body = {
        boardName: values.boardName,
        boardDescription: values.boardDescription || '',
        attachmentYn: ynValue(values.attachmentYn, '허용'),
        useYn: ynValue(values.useYn),
        userId,
      };
      return meta.boardId ? api.put(`/api/boards/${meta.boardId}`, body) : api.post('/api/boards', body);
    },
    postRegister: async () => {
      const body = { boardId, title: values.title, content: values.content || values.title, writerId: userId };
      const postId = meta.postId
        ? (await api.put(`/api/posts/${meta.postId}`, { ...body, userId, admin: isAdminUser(user) }), meta.postId)
        : await api.post('/api/posts', body);
      await uploadFiles(formKey, postId, files, userId);
      return postId;
    },
    reservationRegister: () => {
      const body = {
        resourceId: resource?.resourceId,
        resourceName: values.resourceName,
        resourceType: selectedResourceType,
        requesterId: userId,
        title: values.purpose || '예약 신청',
        purpose: values.purpose || '',
        startDateTime: `${values.reservationDate}T${values.startTime}:00`,
        endDateTime: `${values.reservationDate}T${values.endTime}:00`,
      };
      return meta.reservationId ? api.put(`/api/reservations/${meta.reservationId}`, body) : api.post('/api/reservations', body);
    },
    resourceRegister: () => api.post('/api/reservations/resources', {
      resourceType: values.resourceType === '차량' ? 'VEHICLE' : values.resourceType || 'MEETING_ROOM',
      resourceName: values.resourceName,
      resourceDescription: values.resourceDescription || '',
      capacity: Number(values.capacity) || null,
      location: values.location || '',
      vehicleNumber: values.vehicleNumber || '',
    }),
    templateRegister: () => {
      const body = {
        templateCode: values.templateCode,
        templateName: values.templateName,
        templateDescription: values.templateDescription || '',
        templateContent: values.templateContent || values.templateName,
        useYn: ynValue(values.useYn),
        createdBy: userId,
        updatedBy: userId,
      };
      return meta.templateId ? api.put(`/api/document-templates/${meta.templateId}`, body) : api.post('/api/document-templates', body);
    },
    taskRegister: () => {
      const body = {
        requesterId: userId,
        assigneeId,
        title: values.title,
        content: values.content || '',
        taskStatus: statusValue(values.taskStatus || meta.taskStatus || '예정'),
        priority: values.priority || meta.priority || 'NORMAL',
        startDate: values.startDate || formatDate(new Date().toISOString()),
        dueDate: values.dueDate,
      };
      return meta.taskId ? api.put(`/api/tasks/${meta.taskId}`, body) : api.post('/api/tasks', body);
    },
    scheduleRegister: () => {
      const allDayYn = ynValue(values.allDayYn, '예');
      const body = {
        title: values.title,
        content: values.content || '',
        location: values.location || '',
        scheduleType: values.scheduleType || 'PERSONAL',
        startDatetime: `${values.startDate}T${allDayYn === 'Y' ? '00:00' : values.startTime || '09:00'}:00`,
        endDatetime: `${values.endDate}T${allDayYn === 'Y' ? '23:59' : values.endTime || '10:00'}:00`,
        allDayYn,
      };
      return meta.scheduleId ? api.put(`/api/schedules/${meta.scheduleId}`, body) : api.post('/api/schedules', body);
    },
    messageCompose: () => {
      const receiveId = Number(values.receiveId) || lists.users.rows.find((row) => row[2] === values.receiveId || row[1] === values.receiveId)?._meta?.userId;
      return api.post(`/api/messages?senderId=${userId}`, { receiveId, title: values.title, content: values.content });
    },
    codeRegister: () => {
      const body = {
        codeGroupId: values.codeGroupId,
        codeGroupName: values.codeGroupName,
        description: values.description || '',
        useYn: ynValue(values.useYn),
        createdBy: userId,
        updatedBy: userId,
        details: normalizeCodeDetails(values.details, userId),
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

function normalizeCodeDetails(details = []) {
  return details
    .map((detail, index) => ({
      codeValue: String(detail.codeValue ?? '').trim().toUpperCase(),
      codeName: String(detail.codeName ?? '').trim(),
      sortOrder: Number(detail.sortOrder) || index + 1,
      useYn: ynValue(detail.useYn),
    }))
    .filter((detail) => detail.codeValue && detail.codeName);
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
    templates: () => api.delete(`/api/document-templates/${meta.templateId}?userId=${userId}`),
    tasks: () => api.delete(`/api/tasks/${meta.taskId}`),
    codes: () => api.delete(`/api/common-codes/${meta.codeGroupId}`),
  };
  return handlers[listKey]?.();
}

export async function updateBackendStatus(listKey, row, status, user) {
  const meta = row?._meta ?? {};
  const userId = getCurrentUserId(user);
  if (listKey === 'tasks') return api.patch(`/api/tasks/${meta.taskId}/status`, { taskStatus: statusValue(status) });
  if (listKey === 'approval' && status === '완료') return api.patch(`/api/approvals/${meta.approvalDocumentId ?? meta.approvalId}/approve`, { approverId: userId, comment: '' });
  if (listKey === 'reservations' && ['승인', '반려'].includes(status)) {
    return api.patch(`/api/reservations/${meta.reservationId}/${status === '승인' ? 'approve' : 'reject'}`, {
      approverId: userId,
      approvalComment: '',
    });
  }
  if (listKey === 'reservations' && status === '취소') return api.patch(`/api/reservations/${meta.reservationId}/cancel`);
  return null;
}
