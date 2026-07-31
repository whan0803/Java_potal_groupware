import { Outlet } from 'react-router-dom';
import PageHeader from './PageHeader.jsx';
import Sidebar from './Sidebar.jsx';
import Topbar from './Topbar.jsx';

function AppLayout() {
  return (
    <div className="portal-shell">
      <Topbar />
      <div className="workspace">
        <Sidebar />
        <main className="main-content">
          <PageHeader />
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
