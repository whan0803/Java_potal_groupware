import { Navigate, Route, Routes } from 'react-router-dom';
import FormPage from './components/FormPage.jsx';
import ListPage from './components/ListPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { formScreens } from './data/formScreens.js';
import AppLayout from './layouts/AppLayout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ApprovalApply from './pages/ApprovalApply.jsx';
import ApprovalList from './pages/ApprovalList.jsx';
import Login from './pages/Login.jsx';
import MessageInbox from './pages/MessageInbox.jsx';
import PasswordChange from './pages/PasswordChange.jsx';
import PostDetail from './pages/PostDetail.jsx';
import ReservationApprove from './pages/ReservationApprove.jsx';
import RoleDetail from './pages/RoleDetail.jsx';
import RoleMenuSettings from './pages/RoleMenuSettings.jsx';
import ScheduleList from './pages/ScheduleList.jsx';
import TemplateDetail from './pages/TemplateDetail.jsx';
import UserDetail from './pages/UserDetail.jsx';

function App() {
  return (
    <Routes>
      <Route path="login" element={<Login />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="users" element={<ListPage listKey="users" />} />
        <Route path="users/new" element={<FormPage formKey="userRegister" config={formScreens.userRegister} />} />
        <Route path="users/detail" element={<UserDetail />} />
        <Route path="roles" element={<ListPage listKey="roles" />} />
        <Route path="roles/detail" element={<RoleDetail />} />
        <Route path="roles/new" element={<FormPage formKey="roleRegister" config={formScreens.roleRegister} />} />
        <Route path="roles/menu" element={<RoleMenuSettings />} />
        <Route path="menus" element={<ListPage listKey="menus" />} />
        <Route path="menus/edit" element={<FormPage formKey="menuEdit" config={formScreens.menuEdit} />} />
        <Route path="notices" element={<ListPage listKey="notices" />} />
        <Route path="notices/new" element={<FormPage formKey="noticeRegister" config={formScreens.noticeRegister} />} />
        <Route path="boards" element={<ListPage listKey="boards" />} />
        <Route path="boards/new" element={<FormPage formKey="boardRegister" config={formScreens.boardRegister} />} />
        <Route path="posts" element={<ListPage listKey="posts" />} />
        <Route path="posts/new" element={<FormPage formKey="postRegister" config={formScreens.postRegister} />} />
        <Route path="posts/detail" element={<PostDetail />} />
        <Route path="reservations" element={<ListPage listKey="reservations" />} />
        <Route path="reservations/new" element={<FormPage formKey="reservationRegister" config={formScreens.reservationRegister} />} />
        <Route path="reservations/resources/new" element={<FormPage formKey="resourceRegister" config={formScreens.resourceRegister} />} />
        <Route path="reservations/approve" element={<ReservationApprove />} />
        <Route path="approval" element={<ApprovalList />} />
        <Route path="approval/new" element={<ApprovalApply />} />
        <Route path="templates" element={<ListPage listKey="templates" />} />
        <Route path="templates/new" element={<FormPage formKey="templateRegister" config={formScreens.templateRegister} />} />
        <Route path="templates/detail" element={<TemplateDetail />} />
        <Route path="tasks" element={<ListPage listKey="tasks" />} />
        <Route path="tasks/new" element={<FormPage formKey="taskRegister" config={formScreens.taskRegister} />} />
        <Route path="schedule" element={<ScheduleList />} />
        <Route path="schedule/new" element={<FormPage formKey="scheduleRegister" config={formScreens.scheduleRegister} />} />
        <Route path="messages" element={<MessageInbox />} />
        <Route path="messages/sent" element={<MessageInbox mode="sent" />} />
        <Route path="messages/detail" element={<MessageInbox mode="detail" />} />
        <Route path="messages/empty" element={<MessageInbox mode="empty" />} />
        <Route path="messages/compose" element={<FormPage formKey="messageCompose" config={formScreens.messageCompose} />} />
        <Route path="codes" element={<ListPage listKey="codes" />} />
        <Route path="codes/new" element={<FormPage formKey="codeRegister" config={formScreens.codeRegister} />} />
        <Route path="logs" element={<ListPage listKey="logs" />} />
        <Route path="password" element={<PasswordChange />} />
        <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
