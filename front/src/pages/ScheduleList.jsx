import { useMemo, useState } from 'react';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';

const getScheduleStart = (schedule) =>
  schedule.startDatetime ?? schedule.start_datetime ?? `${schedule.date ?? ''} ${schedule.time ?? '00:00'}:00`;
const getScheduleDate = (schedule) => getScheduleStart(schedule).slice(0, 10);
const getScheduleTime = (schedule) => {
  if ((schedule.allDayYn ?? schedule.all_day_yn) === 'Y') return '종일';
  return getScheduleStart(schedule).slice(11, 16) || schedule.time || '';
};
const getScheduleTypeLabel = (schedule) => ((schedule.scheduleType ?? schedule.schedule_type) === 'PUBLIC' ? '공용' : '개인');

function ScheduleList() {
  const { user, schedules, refreshBackendState } = useApp();
  const [selectedSchedule, setSelectedSchedule] = useState(null);
  const [scheduleValues, setScheduleValues] = useState({});
  const [error, setError] = useState('');
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
    if ((schedule.useYn ?? schedule.use_yn) === 'N') return false;
    const date = new Date(getScheduleDate(schedule));
    return date.getFullYear() === year && date.getMonth() === month;
  });
  const moveMonth = (amount) => {
    setCurrentMonth((current) => new Date(current.getFullYear(), current.getMonth() + amount, 1));
  };
  const openSchedule = async (schedule) => {
    const scheduleId = schedule.scheduleId ?? schedule.schedule_id;
    if (!scheduleId) return;
    try {
      const detail = await api.get(`/api/schedules/${scheduleId}`);
      setSelectedSchedule(detail);
      setScheduleValues({
        title: detail.title ?? '',
        content: detail.content ?? '',
        location: detail.location ?? '',
        startDatetime: String(detail.startDatetime ?? '').slice(0, 16),
        endDatetime: String(detail.endDatetime ?? '').slice(0, 16),
      });
      setError('');
    } catch (loadError) {
      setError(loadError.message || '일정을 불러오지 못했습니다.');
    }
  };
  const saveSchedule = async () => {
    if (!selectedSchedule?.scheduleId) return;
    try {
      await api.put(`/api/schedules/${selectedSchedule.scheduleId}`, {
        title: scheduleValues.title,
        content: scheduleValues.content,
        location: scheduleValues.location,
        startDatetime: `${scheduleValues.startDatetime}:00`,
        endDatetime: `${scheduleValues.endDatetime}:00`,
      });
      await refreshBackendState();
      setSelectedSchedule(null);
    } catch (saveError) {
      setError(saveError.message || '일정 수정에 실패했습니다.');
    }
  };
  const deleteSchedule = async () => {
    if (!selectedSchedule?.scheduleId) return;
    try {
      await api.delete(`/api/schedules/${selectedSchedule.scheduleId}`);
      await refreshBackendState();
      setSelectedSchedule(null);
    } catch (deleteError) {
      setError(deleteError.message || '일정 삭제에 실패했습니다.');
    }
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
            <button className="schedule-row" type="button" key={`${schedule.title}-${getScheduleStart(schedule)}`} onClick={() => openSchedule(schedule)}>
              [{getScheduleTypeLabel(schedule)}] {schedule.title} · {getScheduleDate(schedule)} {getScheduleTime(schedule)}
              {schedule.location ? ` · ${schedule.location}` : ''}
            </button>
          ))
        ) : (
          <p>등록된 일정이 없습니다.</p>
        )}
        <span>일정을 클릭하면 상세 정보가 표시됩니다.</span>
        {error ? <p className="form-error">{error}</p> : null}
      </aside>
      {selectedSchedule ? (
        <aside className="schedule-detail-panel">
          <h2>일정 상세</h2>
          <label className="field">
            <span>제목</span>
            <input value={scheduleValues.title} onChange={(event) => setScheduleValues((current) => ({ ...current, title: event.target.value }))} />
          </label>
          <label className="field large">
            <span>내용</span>
            <textarea value={scheduleValues.content} onChange={(event) => setScheduleValues((current) => ({ ...current, content: event.target.value }))} />
          </label>
          <label className="field">
            <span>장소</span>
            <input value={scheduleValues.location} onChange={(event) => setScheduleValues((current) => ({ ...current, location: event.target.value }))} />
          </label>
          <label className="field">
            <span>시작</span>
            <input type="datetime-local" value={scheduleValues.startDatetime} onChange={(event) => setScheduleValues((current) => ({ ...current, startDatetime: event.target.value }))} />
          </label>
          <label className="field">
            <span>종료</span>
            <input type="datetime-local" value={scheduleValues.endDatetime} onChange={(event) => setScheduleValues((current) => ({ ...current, endDatetime: event.target.value }))} />
          </label>
          <div className="form-actions">
            <button className="button secondary" type="button" onClick={() => setSelectedSchedule(null)}>닫기</button>
            {Number(selectedSchedule.userId) === Number(user?.userId) ? (
              <>
                <button className="button secondary" type="button" onClick={deleteSchedule}>삭제</button>
                <button className="button primary" type="button" onClick={saveSchedule}>저장</button>
              </>
            ) : null}
          </div>
        </aside>
      ) : null}
    </section>
  );
}

export default ScheduleList;
