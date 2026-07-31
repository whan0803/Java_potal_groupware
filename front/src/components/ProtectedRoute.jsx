import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

function ProtectedRoute() {
  const { user } = useApp();
  const location = useLocation();

  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;

  return <Outlet />;
}

export default ProtectedRoute;
