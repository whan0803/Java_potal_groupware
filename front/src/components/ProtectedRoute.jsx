import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth, useGroupwareData } from '../context/AppContext.jsx';
import { canAccessRoute, isAdminUser } from '../utils/permissions.js';

function ProtectedRoute() {
  const { user, apiStatus } = useAuth();
  const { permissions, permissionsLoaded } = useGroupwareData();
  const location = useLocation();

  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (apiStatus.connected && !permissionsLoaded && !isAdminUser(user)) {
    return (
      <section className="content-card">
        <p>권한 정보를 불러오는 중입니다.</p>
      </section>
    );
  }
  if (!canAccessRoute(user, permissions, location.pathname, location.search)) {
    return (
      <section className="content-card">
        <p className="form-error">접근 권한이 없는 메뉴입니다.</p>
        <Link className="button secondary" to="/">
          대시보드로 이동
        </Link>
      </section>
    );
  }

  return <Outlet />;
}

export default ProtectedRoute;
