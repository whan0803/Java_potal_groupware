import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';
import { isAdminUser } from '../utils/permissions.js';

const approvalStatusLabels = {
  DRAFT: '임시저장',
  IN_PROGRESS: '진행중',
  APPROVED: '완료',
  REJECTED: '반려',
  CANCELED: '취소',
};

function ApprovalList() {
  const { user, lists, refreshBackendState } = useApp();
  const [processingId, setProcessingId] = useState(null);
  const [error, setError] = useState('');
  const admin = isAdminUser(user);
  const rows = useMemo(() => {
    const source = lists.approval.rows ?? [];
    if (admin) return source;
    return source.filter((row) => Number(row._meta?.drafterId) === Number(user?.userId));
  }, [admin, lists.approval.rows, user?.userId]);

  const processApproval = async (row, approve) => {
    const documentId = row._meta?.approvalDocumentId;
    if (!documentId) return;
    const comment = approve ? '' : window.prompt('반려 사유를 입력하세요.', '') ?? '';
    setProcessingId(documentId);
    setError('');

    try {
      await api.patch(`/api/approvals/${documentId}/${approve ? 'approve' : 'reject'}`, {
        approverId: user.userId,
        comment,
      });
      await refreshBackendState();
      window.alert(approve ? '승인되었습니다.' : '반려되었습니다.');
    } catch (processError) {
      setError(processError.message || '결재 처리에 실패했습니다.');
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <section className="content-card list-card approval-list">
      <div className="toolbar">
        <div className="tabs">
          <button className="active" type="button">
            {admin ? '전체 결재' : '내 결재 신청'}
          </button>
        </div>
        <Link className="button primary" to="/approval/new">
          결재 신청
        </Link>
      </div>
      {error ? <p className="form-error">{error}</p> : null}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>문서 유형</th>
              <th>제목</th>
              <th>기안자</th>
              <th>기안일</th>
              <th>상태</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            {rows.length ? rows.map((row) => {
              const status = normalizeStatus(row._meta?.approvalStatus ?? row._meta?.status ?? row[6]);
              const canProcess = admin && status === 'IN_PROGRESS';
              const documentId = row._meta?.approvalDocumentId;

              return (
                <tr key={documentId ?? row[0]}>
                  <td>{row[0]}</td>
                  <td>{row[1]}</td>
                  <td>{row[2]}</td>
                  <td>{row[3]}</td>
                  <td>{row[5]}</td>
                  <td><span className={`pill ${approvalStatusLabels[status] ?? row[6]}`}>{approvalStatusLabels[status] ?? row[6]}</span></td>
                  <td>
                    {canProcess ? (
                      <span className="role-action-group">
                        <button className="table-action" type="button" disabled={processingId === documentId} onClick={() => processApproval(row, true)}>
                          승인
                        </button>
                        <button className="table-action danger" type="button" disabled={processingId === documentId} onClick={() => processApproval(row, false)}>
                          반려
                        </button>
                      </span>
                    ) : (
                      <span className="muted-cell">{admin ? '처리 완료' : '처리 권한 없음'}</span>
                    )}
                  </td>
                </tr>
              );
            }) : (
              <tr>
                <td colSpan="7">
                  <div className="empty-panel">결재 문서가 없습니다.</div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function normalizeStatus(status) {
  const labels = {
    임시저장: 'DRAFT',
    진행중: 'IN_PROGRESS',
    완료: 'APPROVED',
    반려: 'REJECTED',
    취소: 'CANCELED',
  };
  return labels[status] ?? status;
}

export default ApprovalList;
