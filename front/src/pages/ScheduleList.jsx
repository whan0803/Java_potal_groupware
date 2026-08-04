import { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext.jsx';

function ScheduleList() {
  const { schedules } = useApp();
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const today = new Date();
  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const days = useMemo(() => {
    const firstDay = new Date(year, month, 1).getDay();
    const lastDate = new Date(year, month + 1, 0).getDate();
    return [
      ...Array.from({ length: firstDay }, (_, index) => ({ key: `empty-${index}`, empty: true })),
      ...Array.from({ length: lastDate }, (_, index) => ({ key: index + 1, day: index + 1 })),
    ];
  }, [year, month]);
  const monthSchedules = schedules.filter((schedule) => {
    const date = new Date(schedule.date);
    return date.getFullYear() === year && date.getMonth() === month;
  });
  const moveMonth = (amount) => {
    setCurrentMonth((current) => new Date(current.getFullYear(), current.getMonth() + amount, 1));
  };

  return (
    <section className="content-card calendar-card">
      <div className="calendar-head">
        <button type="button" onClick={() => moveMonth(-1)}>
          ‹
        </button>
        <strong>
          {year}년 {month + 1}월
        </strong>
        <button type="button" onClick={() => moveMonth(1)}>
          ›
        </button>
      </div>
      <div className="calendar-grid">
        {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
          <b key={day}>{day}</b>
        ))}
        {days.map((item) => (
          <button
            className={item.day === today.getDate() && year === today.getFullYear() && month === today.getMonth() ? 'today' : ''}
            type="button"
            key={item.key}
            disabled={item.empty}
          >
            {item.day ?? ''}
          </button>
        ))}
      </div>
      <aside className="schedule-empty">
        <h2>{month + 1}월 일정 목록</h2>
        {monthSchedules.length ? (
          monthSchedules.map((schedule) => (
            <p key={`${schedule.title}-${schedule.date}`}>
              {schedule.title} · {schedule.date} {schedule.time}
            </p>
          ))
        ) : (
          <p>등록된 일정이 없습니다.</p>
        )}
        <span>일정을 클릭하면 상세 정보가 표시됩니다.</span>
      </aside>
    </section>
  );
}

export default ScheduleList;
