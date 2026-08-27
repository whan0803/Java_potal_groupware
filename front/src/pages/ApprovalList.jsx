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
  const [detail, setDetail] = useState(null);
  const [detailLoadingId, setDetailLoadingId] = useState(null);
  const [error, setError] = useState('');
  const admin = isAdminUser(user);
  const rows = useMemo(() => {
    const source = lists.approval.rows ?? [];
    if (admin) return source;
    return source.filter((row) => Number(row._meta?.drafterId) === Number(user?.userId));
  }, [admin, lists.approval.rows, user?.userId]);

  const openDetail = async (row) => {
    const documentId = row._meta?.approvalDocumentId;
    if (!documentId) return;
    setDetailLoadingId(documentId);
    setError('');

    try {
      const response = await api.get(`/api/approvals/${documentId}`);
      setDetail(response);
    } catch (detailError) {
      setError(detailError.message || '결재 내용을 불러오지 못했습니다.');
    } finally {
      setDetailLoadingId(null);
    }
  };

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
      setDetail(null);
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
              <th>내용</th>
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
                  <td>
                    <button
                      className="table-action"
                      type="button"
                      disabled={detailLoadingId === documentId}
                      onClick={() => openDetail(row)}
                    >
                      {detailLoadingId === documentId ? '로딩' : '보기'}
                    </button>
                  </td>
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
                <td colSpan="8">
                  <div className="empty-panel">결재 문서가 없습니다.</div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {detail ? (
        <ApprovalDetailModal
          detail={detail}
          processing={processingId === detail.approvalDocumentId}
          canProcess={admin && normalizeStatus(detail.approvalStatus) === 'IN_PROGRESS'}
          onClose={() => setDetail(null)}
          onApprove={() => processApproval({ _meta: detail }, true)}
          onReject={() => processApproval({ _meta: detail }, false)}
        />
      ) : null}
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

function ApprovalDetailModal({ detail, processing, canProcess, onClose, onApprove, onReject }) {
  const status = normalizeStatus(detail.approvalStatus);

  return (
    <div className="modal-backdrop">
      <section className="role-modal approval-detail-modal" role="dialog" aria-modal="true" aria-labelledby="approval-detail-title">
        <header>
          <h2 id="approval-detail-title">결재 내용</h2>
          <button type="button" aria-label="닫기" onClick={onClose}>×</button>
        </header>
        <div className="role-modal-body approval-detail-body">
          <dl className="approval-detail-meta">
            <div>
              <dt>문서번호</dt>
              <dd>{detail.documentNumber ?? '-'}</dd>
            </div>
            <div>
              <dt>기안자</dt>
              <dd>{detail.drafterName ?? '-'}</dd>
            </div>
            <div>
              <dt>문서양식</dt>
              <dd>{detail.templateName ?? '선택 안 함'}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd><span className={`pill ${approvalStatusLabels[status] ?? detail.approvalStatus}`}>{approvalStatusLabels[status] ?? detail.approvalStatus}</span></dd>
            </div>
          </dl>
          <div className="approval-detail-content">
            <strong>{detail.title}</strong>
            <p>{detail.content}</p>
          </div>
        </div>
        <footer>
          <button className="button secondary" type="button" onClick={onClose}>닫기</button>
          {canProcess ? (
            <>
              <button className="button primary" type="button" disabled={processing} onClick={onApprove}>승인</button>
              <button className="button danger" type="button" disabled={processing} onClick={onReject}>반려</button>
            </>
          ) : null}
        </footer>
      </section>
    </div>
  );
}

export default ApprovalList;
