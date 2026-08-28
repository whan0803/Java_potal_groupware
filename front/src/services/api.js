const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    headers: {
      ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...(options.headers ?? {}),
    },
    ...options,
  });

  const contentType = response.headers.get('content-type') ?? '';
  const hasJson = contentType.includes('application/json');
  const body = response.status === 204 ? null : hasJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message = body?.message ?? body?.error ?? (body || `API 요청 실패 (${response.status})`);
    throw new ApiError(message, response.status, body);
  }

  return body;
}

const toQuery = (params = {}) => {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value);
  });
  const text = query.toString();
  return text ? `?${text}` : '';
};

export const api = {
  get: (path, params) => request(`${path}${toQuery(params)}`),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: (path, body) => request(path, { method: 'PATCH', body: body === undefined ? undefined : JSON.stringify(body) }),
  delete: (path, body) => request(path, { method: 'DELETE', body: body === undefined ? undefined : JSON.stringify(body) }),
};

export const authApi = {
  login: ({ id, password }) => api.post('/api/auth/login', { loginId: id, password }),
  me: () => api.get('/api/auth/me'),
  changePassword: (body) => api.patch('/api/users/me/password', body),
  logout: () => api.post('/api/auth/logout', {}),
};

export const getPageItems = (response) => response?.content ?? response ?? [];

export const listApi = {
  dashboard: () => api.get('/api/dashboard'),
  users: () => api.get('/api/users', { size: 200 }),
  roles: () => api.get('/api/roles'),
  myRoleMenus: () => api.get('/api/roles/me/menus'),
  roleMenus: (roleId) => api.get(`/api/roles/${roleId}/menus`),
  menus: () => api.get('/api/menu'),
  notices: () => api.get('/api/notices', { size: 200 }),
  boards: () => api.get('/api/boards'),
  posts: () => api.get('/api/posts', { size: 200 }),
  resources: (type) => api.get('/api/reservations/resources', { type }),
  reservations: (resourceId) => api.get('/api/reservations', { resourceId }),
  approvals: () => api.get('/api/approvals'),
  templates: () => api.get('/api/document-templates', { size: 200 }),
  tasks: () => api.get('/api/tasks', { size: 200 }),
  schedules: (start, end) => api.get('/api/schedules/monthly', { start, end }),
  receivedMessages: (userId) => api.get('/api/messages/received', { userId, size: 200 }),
  sentMessages: (userId) => api.get('/api/messages/sent', { userId, size: 200 }),
  codes: () => api.get('/api/common-codes'),
  codeDetails: (codeGroupId, activeOnly = true) => api.get(`/api/common-codes/${codeGroupId}/details`, { activeOnly }),
  auditLogs: () => api.get('/api/audit-logs', { size: 200 }),
};

export const attachmentApi = {
  upload: ({ file, referenceType, referenceId, userId }) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('referenceType', referenceType);
    formData.append('referenceId', referenceId);
    formData.append('userId', userId);
    return request('/api/attachments/upload', { method: 'POST', body: formData });
  },
  list: (referenceType, referenceId) => api.get('/api/attachments', { referenceType, referenceId }),
};
