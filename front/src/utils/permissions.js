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
  if (isAdminUser(user)) return true;

  const permission = getPermissionForPath(permissions, pathname);
  if (!permission) return true;

  const keys = {
    read: 'readYn',
    create: 'createYn',
    update: 'updateYn',
    delete: 'deleteYn',
  };

  return permission[keys[action]] === 'Y';
}

function normalizePath(path = '') {
  if (!path) return '';
  if (path === '/') return '/';
  return path.endsWith('/') ? path.slice(0, -1) : path;
}
