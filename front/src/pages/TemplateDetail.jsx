import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';

function TemplateDetail() {
  const { lists } = useApp();
  const [searchParams] = useSearchParams();
  const index = Number.parseInt(searchParams.get('index') ?? '0', 10);
  const row = lists.templates.rows[index] ?? lists.templates.rows[0];
  const [template, setTemplate] = useState(row?._meta ?? null);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    if (!row?._meta?.templateId) return undefined;
    api.get(`/api/document-templates/${row._meta.templateId}`)
      .then((response) => {
        if (mounted) setTemplate(response);
      })
      .catch((loadError) => {
        if (mounted) setError(loadError.message || '문서양식을 불러오지 못했습니다.');
      });
    return () => {
      mounted = false;
    };
  }, [row]);

  if (!row) {
    return (
      <section className="detail-card">
        <p className="form-error">문서양식을 찾을 수 없습니다.</p>
        <Link className="button secondary" to="/templates">목록</Link>
      </section>
    );
  }

  return (
    <section className="detail-card">
      <div className="detail-head">
        <div>
          <p className="mono">{template?.templateCode ?? row[1]}</p>
          <h2>{template?.templateName ?? row[2]}</h2>
        </div>
        <span className="status-badge">
          <span />
          {(template?.useYn ?? row[4]) === 'N' ? '미사용' : '사용'}
        </span>
      </div>
      <p className="form-help">{template?.templateDescription ?? row[3]}</p>
      <div className="content-preview">{template?.templateContent ?? '내용이 없습니다.'}</div>
      {error ? <p className="form-error">{error}</p> : null}
      <div className="form-actions">
        <Link className="button secondary" to="/templates">목록</Link>
        <Link className="button primary" to={`/templates/new?index=${index}`}>수정</Link>
      </div>
    </section>
  );
}

export default TemplateDetail;
