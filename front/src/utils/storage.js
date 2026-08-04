const storagePrefix = 'groupware-admin';

export function loadStorage(key, fallback) {
  try {
    const rawValue = window.localStorage.getItem(`${storagePrefix}:${key}`);
    return rawValue ? JSON.parse(rawValue) : fallback;
  } catch {
    return fallback;
  }
}

export function saveStorage(key, value) {
  try {
    window.localStorage.setItem(`${storagePrefix}:${key}`, JSON.stringify(value));
  } catch {
    // The app can continue with in-memory state if storage is unavailable.
  }
}
