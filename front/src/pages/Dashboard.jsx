import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

const getScheduleStart = (schedule) =>
  schedule.startDatetime ?? schedule.start_datetime ?? `${schedule.date ?? ''} ${schedule.time ?? '00:00'}:00`;
const getScheduleDate = (schedule) => getScheduleStart(schedule).slice(0, 10);
const getScheduleTime = (schedule) =>
  (schedule.allDayYn ?? schedule.all_day_yn) === 'Y' ? '종일' : getScheduleStart(schedule).slice(11, 16) || schedule.time || '';
const getToday = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

function Dashboard() {
  const { lists, schedules, messages } = useApp();
  const today = getToday();
  const currentMonth = today.slice(0, 7);
  const activeSchedules = schedules.filter((schedule) => (schedule.useYn ?? schedule.use_yn) !== 'N');
  const monthSchedules = activeSchedules.filter((schedule) => getScheduleDate(schedule).startsWith(currentMonth));
  const pick = (rows, mapper, count = 3) => rows.slice(0, count).map(mapper);
  const stats = [
    [String(lists.users.rows.length), '총 사용자', `활성 ${lists.users.rows.filter((row) => row[6] === '사용').length}명`],
    [String(lists.approval.rows.length), '결재 대기', '처리 필요'],
    [String(lists.reservations.rows.filter((row) => row[8] === '대기').length), '예약 승인 대기', '승인 대기'],
    [String(lists.tasks.rows.filter((row) => row[5] === '진행중').length), '진행 중 업무', '현재 진행'],
    [String(monthSchedules.length), '이번 달 일정', '등록된 일정'],
    [String(messages.length), '읽지 않은 쪽지', '미확인'],
  ];
  const panels = [
    ['중요 공지사항', '/notices', pick(lists.notices.rows.filter((row) => row[1] === '중요'), (row) => [row[2], `${row[3]} · ${row[6]}`])],
    ['결재 대기함', '/approval', pick(lists.approval.rows, (row) => [row[2], `${row[3]} · ${row[4]} · ${row[5]}`])],
    ['예약 승인 대기', '/reservations/approve', pick(lists.reservations.rows.filter((row) => row[8] === '대기'), (row) => [row[2], `${row[3]} · ${row[5]} ${row[6]}`])],
    ['진행 중 업무', '/tasks', pick(lists.tasks.rows.filter((row) => row[5] === '진행중'), (row) => [`${row[1]} ${row[6]}`, `${row[2]} · ${row[4]}`])],
    ['오늘 일정', '/schedule', pick(activeSchedules.filter((schedule) => getScheduleDate(schedule) === today), (schedule) => [schedule.title, `${getScheduleDate(schedule)} ${getScheduleTime(schedule)}`])],
    ['받은 쪽지', '/messages', pick(messages, (message) => [message[2], `${message[0]} · ${message[3]} · ${message[1]}`])],
  ];
  const activityRows = lists.logs.rows.slice(0, 5);

  return (
    <div className="dashboard">
      <div className="stat-grid">
        {stats.map(([count, label, note]) => (
          <article className="stat-card" key={label}>
            <strong>{count}</strong>
            <span>{label}</span>
            <p>{note}</p>
          </article>
        ))}
      </div>
      <div className="panel-grid">
        {panels.map(([title, path, items]) => (
          <section className="content-card panel" key={title}>
            <div className="card-title">
              <h2>{title}</h2>
              <Link to={path}>전체보기</Link>
            </div>
            {items.length ? items.map(([title, meta]) => (
              <div className="feed-row" key={`${title}-${meta}`}>
                <strong>{title}</strong>
                <span>{meta}</span>
              </div>
            )) : <div className="feed-row"><span>표시할 데이터가 없습니다.</span></div>}
          </section>
        ))}
      </div>
      <section className="content-card activity">
        <div className="card-title">
          <h2>최근 변경 이력</h2>
        </div>
        {activityRows.length ? activityRows.map((row) => (
            <div className="activity-row" key={row[0]}>
              <time>{String(row[4]).split(' ').at(-1)}</time>
              <span>{row[2]} — {row[5]}</span>
            </div>
          )) : <div className="activity-row"><span>최근 변경 이력이 없습니다.</span></div>}
      </section>
    </div>
  );
}

export default Dashboard;
