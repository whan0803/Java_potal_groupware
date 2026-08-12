import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';

function ApprovalApply() {
  const navigate = useNavigate();
  const { user, lists, refreshBackendState } = useApp();
  const approvers = useMemo(
    () => lists.users.rows
      .filter((row) =>
        row[7] !== '미사용' &&
        Number(row._meta?.userId) !== Number(user?.userId) &&
        row._meta?.roles?.some((role) => ['ROLE_ADMIN', 'ADMIN'].includes(role.roleCode)),
      )
      .map((row) => ({ userId: row._meta?.userId, name: `${row[2]} (${row[1]})` })),
    [lists.users.rows, user],
  );
  const templates = useMemo(
    () => lists.templates.rows
      .filter((row) => row[4] !== '미사용')
      .map((row) => ({ templateId: row._meta?.templateId, name: `${row[2]} (${row[1]})` })),
    [lists.templates.rows],
  );
  const [values, setValues] = useState({
    templateId: '',
    title: '',
    content: '',
    approverId: '',
  });
  const [error, setError] = useState('');

  const change = (key, value) => {
    setValues((current) => ({ ...current, [key]: value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!values.title.trim() || !values.content.trim() || !values.approverId) {
      setError('제목, 내용, 결재자를 입력하세요.');
      return;
    }

    try {
      const documentId = await api.post('/api/approvals', {
        templateId: Number(values.templateId) || null,
        drafterId: user.userId,
        title: values.title,
        content: values.content,
        approvalLines: [
          {
            approverId: Number(values.approverId),
            approvalOrder: 1,
            approvalType: 'APPROVAL',
          },
        ],
      });
      await api.patch(`/api/approvals/${documentId}/submit`, { userId: user.userId });
      await refreshBackendState();
      window.alert('결재 신청이 상신되었습니다.');
      navigate('/approval');
    } catch (submitError) {
      setError(submitError.message || '결재 신청에 실패했습니다.');
    }
  };

  return (
    <form className="content-card form-card medium" onSubmit={submit}>
      <div className="field-grid">
        <label className="field">
          <span>문서양식</span>
          <select value={values.templateId} onChange={(event) => change('templateId', event.target.value)}>
            <option value="">선택 안 함</option>
            {templates.map((template) => (
              <option value={template.templateId} key={template.templateId}>
                {template.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span>결재자<b>*</b></span>
          <select required value={values.approverId} onChange={(event) => change('approverId', event.target.value)}>
            <option value="">선택</option>
            {approvers.map((approver) => (
              <option value={approver.userId} key={approver.userId}>
                {approver.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field large">
          <span>제목<b>*</b></span>
          <input value={values.title} onChange={(event) => change('title', event.target.value)} />
        </label>
        <label className="field large">
          <span>내용<b>*</b></span>
          <textarea value={values.content} onChange={(event) => change('content', event.target.value)} />
        </label>
      </div>
      {error ? <p className="form-error">{error}</p> : null}
      <div className="form-actions">
        <button className="button secondary" type="button" onClick={() => navigate('/approval')}>취소</button>
        <button className="button primary" type="submit">상신</button>
      </div>
    </form>
  );
}

export default ApprovalApply;
