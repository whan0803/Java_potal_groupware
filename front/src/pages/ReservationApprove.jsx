import DataTable from '../components/DataTable.jsx';
import { useApp } from '../context/AppContext.jsx';

function ReservationApprove() {
  const { lists, updateRowStatus } = useApp();
  const waitingCount = lists.reservations.rows.filter((row) => row[8] === '대기').length;

  return (
    <section className="content-card list-card">
      <div className="approve-banner">승인 대기: {waitingCount}건</div>
      <DataTable
        columns={['유형', '자원명', '신청자', '부서', '예약일', '시간', '목적', '신청일', '상태', '처리']}
        rows={lists.reservations.rows.map((row) => [
          row[1],
          row[2],
          row[3],
          row[4],
          row[5],
          row[6],
          row[7],
          row[0],
          row[8],
          row[8] === '대기' ? '승인 / 반려' : '처리 완료',
        ])}
        onAction={async (row) => {
          if (row[8] !== '대기') return;
          const rowIndex = lists.reservations.rows.findIndex((reservation) => reservation[0] === row[7]);
          if (rowIndex < 0) return;
          try {
            await updateRowStatus('reservations', rowIndex, '승인');
            window.alert('예약이 승인 처리되었습니다.');
          } catch (error) {
            window.alert(error.message || '처리 중 오류가 발생했습니다.');
          }
        }}
      />
    </section>
  );
}

export default ReservationApprove;
