import { useApp } from '../context/AppContext.jsx';

function Dashboard() {
  const { lists, schedules, messages } = useApp();
  const stats = [
    [String(lists.users.rows.length), '총 사용자', `활성 ${lists.users.rows.filter((row) => row[6] === '사용').length}명`],
    [String(lists.approval.rows.length), '결재 대기', '처리 필요'],
    [String(lists.reservations.rows.filter((row) => row[8] === '대기').length), '예약 승인 대기', '승인 대기'],
    [String(lists.tasks.rows.filter((row) => row[5] === '진행중').length), '진행 중 업무', '이번 주 마감 1건'],
    [String(schedules.length), '이번 달 일정', '등록된 일정'],
    [String(messages.length), '읽지 않은 쪽지', '미확인'],
  ];
  const panels = [
    ['중요 공지사항', ['2024년 하반기 보안 정책 변경 안내', '그룹웨어 시스템 점검 공지']],
    ['결재 대기함', ['2024년 3분기 IT 예산 집행 승인 요청', '해외 출장 신청서 — 일본 도쿄']],
    ['예약 승인 대기', ['법인차량 001호', '교육실']],
    ['진행 중 업무', ['그룹웨어 사용자 매뉴얼 작성 65%', '신규 거래처 영업 제안서 작성 40%']],
    ['오늘 일정', ['분기 경영 전략 회의', 'IT 시스템 정기 점검']],
    ['받은 쪽지', ['7월 인사 발령 안내', '서버 점검 협조 요청', '재무 시스템 오류 문의']],
  ];

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
        {panels.map(([title, items]) => (
          <section className="content-card panel" key={title}>
            <div className="card-title">
              <h2>{title}</h2>
              <button type="button">전체보기</button>
            </div>
            {items.map((item) => (
              <div className="feed-row" key={item}>
                <strong>{item}</strong>
                <span>홍길동 · IT기획팀 · 2024-07-25</span>
              </div>
            ))}
          </section>
        ))}
      </div>
      <section className="content-card activity">
        <div className="card-title">
          <h2>최근 변경 이력</h2>
        </div>
        {['사용자 수정 — kim.jisoo', '권한별 메뉴 저장 — ROLE_USER', '예약 승인 — 대회의실 A', '공지사항 등록 — 그룹웨어 점검 공지', '업무 상태 변경 — IT 인프라 점검'].map(
          (item, index) => (
            <div className="activity-row" key={item}>
              <time>{['14:20', '11:05', '16:30', '10:15', '09:00'][index]}</time>
              <span>{item}</span>
            </div>
          ),
        )}
      </section>
    </div>
  );
}

export default Dashboard;
