import DataTable from '../components/DataTable.jsx';
import { useApp } from '../context/AppContext.jsx';

const toApprovalRow = (reservation) => {
  const [no, type, resourceName, requester, department, date, time, purpose, status] = reservation;

  return {
    no,
    status,
    cells: [
      type,
      resourceName,
      requester,
      department,
      date,
      time,
      purpose,
      no,
      status,
      status === '대기' ? '승인 / 반려' : '처리 완료',
    ],
  };
};

function ReservationApprove() {
  const { lists, updateRowStatus } = useApp();
  const approvalRows = lists.reservations.rows.map(toApprovalRow);
  const waitingCount = approvalRows.filter(({ status }) => status === '대기').length;
  const reservationRows = approvalRows.map(({ cells }) => cells);

  return (
    <section className="content-card list-card">
      <div className="approve-banner">승인 대기: {waitingCount}건</div>
      <DataTable
        columns={['유형', '자원명', '신청자', '부서', '예약일', '시간', '목적', '신청일', '상태', '처리']}
        rows={reservationRows}
        onAction={async (row) => {
          const selected = approvalRows.find(({ cells }) => cells === row);
          if (!selected || selected.status !== '대기') return;
          const rowIndex = lists.reservations.rows.findIndex(([no]) => no === selected.no);
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
