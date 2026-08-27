export function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export function getPermissionForPath(permissions = [], pathname = '') {
  const normalizedPath = normalizePath(pathname);
  return permissions.find((permission) => normalizePath(permission.menuUrl) === normalizedPath) ?? null;
}

export function canUsePermission(user, permissions, pathname, action = 'read') {
  if (isAdminUser(user)) return true;

  const keys = {
    read: 'readYn',
    create: 'createYn',
    update: 'updateYn',
    delete: 'deleteYn',
  };
  const normalizedPath = normalizePath(pathname);
  if (normalizedPath === '/') return true;
  if (normalizedPath === '/password') return true;
  if (!permissions.length) return !user?.userId;

  const permission = getPermissionForPathAliases(permissions, pathname);
  if (!permission) return false;

  return permission[keys[action]] === 'Y';
}

export function canAccessRoute(user, permissions, pathname, search = '') {
  return canUsePermission(user, permissions, pathname, getRouteAction(pathname, search));
}

export function canOpenAction(user, permissions, targetPath) {
  const [pathname, query = ''] = targetPath.split('?');
  const search = query ? `?${query}` : '';
  return canUsePermission(user, permissions, pathname, getRouteAction(pathname, search));
}

function getRouteAction(pathname, search = '') {
  if (isFormRoute(pathname)) {
    return new URLSearchParams(search).has('index') ? 'update' : 'create';
  }
  return 'read';
}

function getPermissionForPathAliases(permissions, pathname) {
  const paths = getPermissionPaths(pathname);
  const pathPermission = paths.map((path) => getPermissionForPath(permissions, path)).find(Boolean);
  if (pathPermission) return pathPermission;

  const names = getPermissionNames(pathname);
  return permissions.find((permission) => names.includes(permission.menuName)) ?? null;
}

function getPermissionPaths(pathname) {
  const normalizedPath = normalizePath(pathname);
  const aliases = {
    '/users': ['/users', '/users/list'],
    '/users/new': ['/users/new', '/users', '/users/list'],
    '/users/detail': ['/users/detail', '/users', '/users/list'],
    '/roles': ['/roles', '/roles/list'],
    '/roles/new': ['/roles/new', '/roles', '/roles/list'],
    '/roles/detail': ['/roles/detail', '/roles', '/roles/list'],
    '/roles/menu': ['/roles/menu', '/roles', '/roles/list'],
    '/menus': ['/menus', '/menus/list'],
    '/menus/edit': ['/menus/edit', '/menus', '/menus/list'],
    '/notices': ['/notices', '/notices/list'],
    '/notices/new': ['/notices/new', '/notices', '/notices/list'],
    '/boards': ['/boards', '/boards/list'],
    '/boards/new': ['/boards/new', '/boards', '/boards/list'],
    '/posts': ['/posts', '/posts/list'],
    '/posts/new': ['/posts/new', '/posts', '/posts/list'],
    '/posts/detail': ['/posts/detail', '/posts', '/posts/list'],
    '/reservations': ['/reservations', '/rsv/list'],
    '/reservations/new': ['/reservations/new', '/rsv/new', '/reservations', '/rsv/list'],
    '/reservations/approve': ['/reservations/approve', '/rsv/approve', '/reservations', '/rsv/list'],
    '/approval': ['/approval', '/apr/inbox'],
    '/approval/new': ['/approval/new', '/approval', '/apr/inbox'],
    '/templates': ['/templates', '/templates/list'],
    '/templates/new': ['/templates/new', '/templates', '/templates/list'],
    '/templates/detail': ['/templates/detail', '/templates', '/templates/list'],
    '/tasks': ['/tasks', '/tasks/list'],
    '/tasks/new': ['/tasks/new', '/tasks', '/tasks/list'],
    '/schedule': ['/schedule', '/schedule/list'],
    '/schedule/new': ['/schedule/new', '/schedule', '/schedule/list'],
    '/messages': ['/messages', '/messages/inbox'],
    '/messages/sent': ['/messages/sent', '/messages'],
    '/messages/detail': ['/messages/detail', '/messages'],
    '/messages/empty': ['/messages/empty', '/messages'],
    '/messages/compose': ['/messages/compose', '/messages'],
    '/codes': ['/codes', '/codes/list'],
    '/codes/new': ['/codes/new', '/codes', '/codes/list'],
    '/logs': ['/logs', '/audit-logs'],
    '/password': ['/password'],
  };
  return aliases[normalizedPath] ?? [normalizedPath];
}

function getPermissionNames(pathname) {
  const normalizedPath = normalizePath(pathname);
  const aliases = {
    '/users': ['사용자 목록', '사용자 관리'],
    '/users/new': ['사용자 등록', '사용자 목록', '사용자 관리'],
    '/users/detail': ['사용자 상세', '사용자 목록', '사용자 관리'],
    '/roles': ['권한 목록', '권한 관리'],
    '/roles/new': ['권한 등록', '권한 목록', '권한 관리'],
    '/roles/detail': ['권한 상세', '권한 목록', '권한 관리'],
    '/roles/menu': ['권한별 메뉴 설정'],
    '/menus': ['메뉴 목록', '메뉴 관리', '포털관리'],
    '/menus/edit': ['메뉴 등록', '메뉴 목록', '메뉴 관리', '포털관리'],
    '/notices': ['공지사항 목록', '공지사항'],
    '/notices/new': ['공지 등록', '공지사항 목록', '공지사항'],
    '/boards': ['게시판 목록', '게시판 관리'],
    '/boards/new': ['게시판 등록', '게시판 목록', '게시판 관리'],
    '/posts': ['게시글 목록', '게시판 관리'],
    '/posts/new': ['게시글 등록', '게시글 목록', '게시판 관리'],
    '/posts/detail': ['게시글 상세', '게시글 목록', '게시판 관리'],
    '/reservations': ['예약 목록', '예약 관리'],
    '/reservations/new': ['예약 신청', '예약 목록', '예약 관리'],
    '/reservations/resources/new': ['예약 자원 등록', '예약 관리'],
    '/reservations/approve': ['예약 승인', '예약 관리'],
    '/approval': ['결재 목록', '전자결재'],
    '/approval/new': ['결재 신청', '전자결재'],
    '/templates': ['문서양식 목록', '문서양식 관리'],
    '/templates/new': ['문서양식 등록', '문서양식 목록', '문서양식 관리'],
    '/templates/detail': ['문서양식 상세', '문서양식 목록', '문서양식 관리'],
    '/tasks': ['업무 목록', '업무 관리'],
    '/tasks/new': ['업무 등록', '업무 목록', '업무 관리'],
    '/schedule': ['일정 목록', '일정 관리'],
    '/schedule/new': ['일정 등록', '일정 목록', '일정 관리'],
    '/messages': ['받은 쪽지', '쪽지함', '쪽지'],
    '/messages/sent': ['보낸 쪽지', '쪽지'],
    '/messages/detail': ['쪽지 상세', '쪽지'],
    '/messages/empty': ['빈 쪽지함', '쪽지'],
    '/messages/compose': ['쪽지 작성', '쪽지'],
    '/codes': ['공통코드 목록', '공통코드 관리', '시스템 관리'],
    '/codes/new': ['공통코드 등록', '공통코드 목록', '공통코드 관리'],
    '/logs': ['감사 로그'],
  };
  return aliases[normalizedPath] ?? [];
}

function isFormRoute(pathname) {
  return [
    '/users/new',
    '/roles/new',
    '/menus/edit',
    '/notices/new',
    '/boards/new',
    '/posts/new',
    '/reservations/new',
    '/reservations/resources/new',
    '/approval/new',
    '/templates/new',
    '/tasks/new',
    '/schedule/new',
    '/messages/compose',
    '/codes/new',
  ].includes(pathname);
}

function normalizePath(path = '') {
  const [pathname] = path.split(/[?#]/);
  path = pathname;
  if (!path) return '';
  if (path === '/') return '/';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}
