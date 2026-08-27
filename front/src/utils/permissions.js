export function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export function getPermissionForPath(permissions = [], pathname = '') {
  return getPermissionsForPath(permissions, pathname)[0] ?? null;
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
  if (!permissions.length) return false;

  const relatedPermissions = getPermissionsForPathAliases(permissions, pathname);
  if (!relatedPermissions.length) return false;

  return relatedPermissions.some((permission) => isAllowed(permission[keys[action]]));
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

function getPermissionsForPathAliases(permissions, pathname) {
  const paths = getPermissionPaths(pathname);
  return uniquePermissions(paths.flatMap((path) => getPermissionsForPath(permissions, path)));
}

function uniquePermissions(permissions) {
  return [...new Map(permissions.map((permission) => [permission.menuId ?? permission.menuUrl ?? permission.menuName, permission])).values()];
}

function getPermissionsForPath(permissions = [], pathname = '') {
  const normalizedPath = normalizePath(pathname);
  return permissions.filter((permission) => normalizePath(permission.menuUrl) === normalizedPath);
}

function isAllowed(value) {
  return value === true || String(value ?? '').trim().toUpperCase() === 'Y';
}

function getPermissionPaths(pathname) {
  const normalizedPath = normalizePath(pathname);
  const aliases = {
    '/users': ['/users', '/users/list', '/api/users'],
    '/users/new': ['/users/new', '/api/users/new', '/users', '/users/list', '/api/users'],
    '/users/detail': ['/users/detail', '/api/users/detail', '/users', '/users/list', '/api/users'],
    '/roles': ['/roles', '/roles/list', '/api/roles'],
    '/roles/new': ['/roles/new', '/api/roles/new', '/roles', '/roles/list', '/api/roles'],
    '/roles/detail': ['/roles/detail', '/api/roles/detail', '/roles', '/roles/list', '/api/roles'],
    '/roles/menu': ['/roles/menu', '/api/roles/menu', '/roles', '/roles/list', '/api/roles'],
    '/menus': ['/menus', '/menus/list', '/api/menu'],
    '/menus/edit': ['/menus/edit', '/api/menu/edit', '/menus', '/menus/list', '/api/menu'],
    '/notices': ['/notices', '/notices/list', '/api/notices'],
    '/notices/new': ['/notices/new', '/api/notices/new', '/notices', '/notices/list', '/api/notices'],
    '/boards': ['/boards', '/boards/list', '/api/boards'],
    '/boards/new': ['/boards/new', '/api/boards/new', '/boards', '/boards/list', '/api/boards'],
    '/posts': ['/posts', '/posts/list', '/api/posts'],
    '/posts/new': ['/posts/new', '/api/posts/new', '/posts', '/posts/list', '/api/posts'],
    '/posts/detail': ['/posts/detail', '/api/posts/detail', '/posts', '/posts/list', '/api/posts'],
    '/reservations': ['/reservations', '/rsv/list', '/api/reservations'],
    '/reservations/new': ['/reservations/new', '/rsv/new', '/api/reservations/new', '/reservations', '/rsv/list', '/api/reservations'],
    '/reservations/approve': ['/reservations/approve', '/rsv/approve', '/api/reservations/approve', '/reservations', '/rsv/list', '/api/reservations'],
    '/approval': ['/approval', '/apr/inbox', '/api/approvals'],
    '/approval/new': ['/approval/new', '/api/approvals/new', '/approval', '/apr/inbox', '/api/approvals'],
    '/templates': ['/templates', '/templates/list', '/document-templates', '/api/document-templates'],
    '/templates/new': ['/templates/new', '/document-templates/new', '/api/document-templates/new', '/templates', '/templates/list', '/document-templates', '/api/document-templates'],
    '/templates/detail': ['/templates/detail', '/document-templates/detail', '/api/document-templates/detail', '/templates', '/templates/list', '/document-templates', '/api/document-templates'],
    '/tasks': ['/tasks', '/tasks/list', '/api/tasks'],
    '/tasks/new': ['/tasks/new', '/api/tasks/new', '/tasks', '/tasks/list', '/api/tasks'],
    '/schedule': ['/schedule', '/schedule/list', '/api/schedules'],
    '/schedule/new': ['/schedule/new', '/api/schedules/new', '/schedule', '/schedule/list', '/api/schedules'],
    '/messages': ['/messages', '/messages/inbox', '/api/messages'],
    '/messages/sent': ['/messages/sent', '/api/messages/sent', '/messages', '/api/messages'],
    '/messages/detail': ['/messages/detail', '/api/messages/detail', '/messages', '/api/messages'],
    '/messages/empty': ['/messages/empty', '/messages', '/api/messages'],
    '/messages/compose': ['/messages/compose', '/api/messages/compose', '/messages', '/api/messages'],
    '/codes': ['/codes', '/codes/list', '/api/common-codes'],
    '/codes/new': ['/codes/new', '/api/common-codes/new', '/codes', '/codes/list', '/api/common-codes'],
    '/logs': ['/logs', '/audit-logs', '/api/audit-logs'],
    '/password': ['/password'],
  };
  return aliases[normalizedPath] ?? [normalizedPath];
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
  if (/^https?:\/\//i.test(path)) {
    try {
      path = new URL(path).pathname;
    } catch {
      return '';
    }
  }
  if (path === '/') return '/';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}
