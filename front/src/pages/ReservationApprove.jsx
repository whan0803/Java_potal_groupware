import DataTable from '../components/DataTable.jsx';
import { useApp } from '../context/AppContext.jsx';

function ReservationApprove() {
  const { updateRowStatus } = useApp();

  return (
    <section className="content-card list-card">
      <div className="approve-banner">승인 대기: 2건</div>
      <DataTable
        columns={['유형', '자원명', '신청자', '부서', '예약일', '시간', '목적', '신청일', '상태', '처리']}
        rows={[
          ['회의실', '대회의실 A', '홍길동', 'IT기획팀', '2024-07-30', '10:00~12:00', '분기 사업 계획 보고', '2024-07-25', '승인', '처리 완료'],
          ['차량', '법인차량 001호', '이민호', '영업팀', '2024-07-31', '09:00~18:00', '거래처 방문 및 영업', '2024-07-26', '대기', '승인 / 반려'],
          ['회의실', '소회의실 B', '박수지', '마케팅팀', '2024-07-29', '14:00~15:00', '마케팅 전략 회의', '2024-07-24', '반려', '처리 완료'],
          ['회의실', '교육실', '정유나', '재무팀', '2024-08-01', '13:00~17:00', '신규 회계 시스템 교육', '2024-07-27', '대기', '승인 / 반려'],
          ['차량', '법인차량 002호', '김지수', '인사팀', '2024-08-01', '10:00~14:00', '채용 면접 장소 이동', '2024-07-27', '대기', '승인 / 반려'],
        ]}
        onAction={(row) => {
          if (row[8] !== '대기') return;
          window.alert('예약이 승인 처리되었습니다.');
          updateRowStatus('reservations', 1, '승인');
        }}
      />
    </section>
  );
}

export default ReservationApprove;
