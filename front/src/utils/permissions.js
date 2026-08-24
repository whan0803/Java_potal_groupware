export function isAdminUser(user) {
  const roles = [user?.role, ...(user?.roles ?? [])];
  return roles.some((role) => ['시스템 관리자', 'ROLE_ADMIN', 'ADMIN'].includes(role));
}

export function getPermissionForPath(permissions = [], pathname = '') {
  const normalizedPath = normalizePath(pathname);
  const candidates = permissions.filter((permission) => {
    const menuPath = normalizePath(permission.menuUrl);
    return menuPath && (normalizedPath === menuPath || normalizedPath.startsWith(`${menuPath}/`));
  });

  return candidates.sort((a, b) => normalizePath(b.menuUrl).length - normalizePath(a.menuUrl).length)[0] ?? null;
}

export function canUsePermission(user, permissions, pathname, action = 'read') {
  const keys = {
    read: 'readYn',
    create: 'createYn',
    update: 'updateYn',
    delete: 'deleteYn',
  };
  const relatedPermissions = getRelatedPermissions(permissions, pathname);

  if (!relatedPermissions.length) return true;

  return relatedPermissions.some((permission) => permission[keys[action]] === 'Y');
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
  if (pathname === '/roles/menu') return 'update';
  if (isFormRoute(pathname)) {
    return new URLSearchParams(search).has('index') ? 'update' : 'create';
  }
  return 'read';
}

function getRelatedPermissions(permissions, pathname) {
  const paths = getPermissionPaths(pathname);
  const matched = paths.map((path) => getPermissionForPath(permissions, path)).filter(Boolean);
  return [...new Map(matched.map((permission) => [permission.menuId ?? permission.menuUrl, permission])).values()];
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
    '/menus': ['/menus', '/menus/list'],
    '/menus/edit': ['/menus/edit', '/menus', '/menus/list'],
    '/notices': ['/notices', '/notices/list'],
    '/notices/new': ['/notices/new', '/notices', '/notices/list'],
    '/reservations': ['/reservations', '/rsv/list'],
    '/reservations/new': ['/reservations/new', '/rsv/new', '/reservations', '/rsv/list'],
    '/reservations/approve': ['/reservations/approve', '/rsv/approve', '/reservations', '/rsv/list'],
    '/approval': ['/approval', '/apr/inbox'],
    '/approval/new': ['/approval/new', '/approval', '/apr/inbox'],
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
  if (path === '/') return '/';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}
