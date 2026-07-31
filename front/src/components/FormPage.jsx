import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { buildRow, formTargets } from '../utils/formActions.js';
import RoleSelectModal from './RoleSelectModal.jsx';

function FormPage({ formKey, config }) {
  const navigate = useNavigate();
  const { lists, addListRow, addSchedule, addMessage } = useApp();
  const fields = config.fields ?? [];
  const [values, setValues] = useState({});
  const [selectedChip, setSelectedChip] = useState(config.chips?.[0] ?? '');
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState('');

  const handleChange = (label, value) => {
    setValues((current) => ({ ...current, [label]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const validation = validateForm(config, values, lists);
    if (!validation.ok) {
      setError(validation.message);
      return;
    }

    const target = formTargets[formKey];
    if (!target) return;

    if (target.listKey) {
      const row = buildRow(formKey, { ...values, '예약 유형': selectedChip }, lists[target.listKey].rows.length);
      if (row) addListRow(target.listKey, row);
    }

    if (formKey === 'messageCompose') {
      addMessage(['홍길동', '방금', values.제목 || '새 쪽지', 'IT기획팀']);
    }

    if (formKey === 'scheduleRegister') {
      addSchedule({
        title: values.제목 || '신규 일정',
        date: values.시작일 || '2026-07-28',
        time: values['시작 시간'] || '09:00',
      });
    }

    setError('');
    window.alert(config.action === '보내기' ? '발송 완료' : '저장되었습니다.');
    navigate(target.redirectTo);
  };

  return (
    <form className={`content-card form-card ${config.width ?? ''}`} onSubmit={handleSubmit}>
      {config.chips ? (
        <div className="choice-row">
          {config.chips.map((chip, index) => (
            <button
              className={selectedChip === chip ? 'active' : ''}
              type="button"
              key={chip}
              onClick={() => setSelectedChip(chip)}
            >
              {chip}
            </button>
          ))}
        </div>
      ) : null}
      {config.subtitle ? <h2 className="section-heading">{config.subtitle}</h2> : null}
      {formKey === 'userRegister' ? (
        <div className="selected-role-strip">
          <strong>선택된 권한</strong>
          <span>{selectedRoles.length ? selectedRoles.join(', ') : '선택된 권한이 없습니다.'}</span>
        </div>
      ) : null}
      {config.sections
        ? config.sections.map((section) => (
            <FormSection
              section={section}
              values={values}
              onChange={handleChange}
              onRoleSelect={() => setIsRoleModalOpen(true)}
              selectedRoles={selectedRoles}
              key={section.title}
            />
          ))
        : null}
      {fields.length ? (
        <FieldGrid fields={fields} placeholders={config.placeholders} values={values} onChange={handleChange} />
      ) : null}
      {config.help ? <p className="form-help">{config.help}</p> : null}
      {config.upload ? <UploadBox upload={config.upload} files={files} onFilesChange={setFiles} /> : null}
      {error ? <p className="form-error">{error}</p> : null}
      <div className="form-actions">
        <button className="button secondary" type="button" onClick={() => navigate(formTargets[formKey]?.redirectTo ?? '/')}>
          취소
        </button>
        {config.action === '중복 확인' ? (
          <button className="button secondary" type="button">
            중복 확인
          </button>
        ) : null}
        <button className="button primary" type="submit">
          {config.action === '보내기' ? '보내기' : '저장'}
        </button>
      </div>
      {isRoleModalOpen ? (
        <RoleSelectModal
          selectedRoles={selectedRoles}
          onClose={() => setIsRoleModalOpen(false)}
          onConfirm={(roles) => {
            setSelectedRoles(roles);
            setIsRoleModalOpen(false);
          }}
        />
      ) : null}
    </form>
  );
}

function validateForm(config, values, lists) {
  const fields = [
    ...(config.fields ?? []),
    ...(config.sections?.flatMap((section) => section.fields ?? []) ?? []),
  ];
  const required = fields.filter((field) => field.includes('*')).map((field) => field.replace(' *', ''));
  const missing = required.find((field) => !values[field]?.trim());
  if (missing) return { ok: false, message: `${missing} 항목은 필수입니다.` };

  if (values.이메일 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.이메일)) {
    return { ok: false, message: '이메일 형식이 올바르지 않습니다.' };
  }
  if (values.연락처 && !/^010-\d{4}-\d{4}$/.test(values.연락처)) {
    return { ok: false, message: '연락처는 010-0000-0000 형식이어야 합니다.' };
  }
  if (values['게시 시작일'] && values['게시 종료일'] && values['게시 종료일'] < values['게시 시작일']) {
    return { ok: false, message: '게시 종료일은 시작일보다 이전일 수 없습니다.' };
  }
  if (values.시작일 && values.종료일 && values.종료일 < values.시작일) {
    return { ok: false, message: '종료일은 시작일보다 이전일 수 없습니다.' };
  }
  if (values.아이디 && lists.users.rows.some((row) => row[1] === values.아이디)) {
    return { ok: false, message: '이미 사용 중인 아이디입니다.' };
  }
  if (values.이메일 && lists.users.rows.some((row) => row[4] === values.이메일)) {
    return { ok: false, message: '이미 사용 중인 이메일입니다.' };
  }
  if (values['권한 코드'] && lists.roles.rows.some((row) => row[1] === values['권한 코드'])) {
    return { ok: false, message: '이미 사용 중인 권한 코드입니다.' };
  }
  if (values['양식 코드'] && lists.templates.rows.some((row) => row[1] === values['양식 코드'])) {
    return { ok: false, message: '이미 사용 중인 양식 코드입니다.' };
  }
  if (values['코드 그룹 ID'] && lists.codes.rows.some((row) => row[1] === values['코드 그룹 ID'])) {
    return { ok: false, message: '이미 사용 중인 코드 그룹 ID입니다.' };
  }

  return { ok: true };
}

function FormSection({ section, values, onChange, onRoleSelect, selectedRoles = [] }) {
  return (
    <section className="form-section">
      <h2>{section.title}</h2>
      {section.fields ? <FieldGrid fields={section.fields} values={values} onChange={onChange} /> : null}
      {section.empty ? (
        <div className="empty-strip">
          {selectedRoles.length ? (
            <div className="selected-role-list">
              {selectedRoles.map((role) => (
                <span className="role-chip" key={role}>
                  {role}
                </span>
              ))}
            </div>
          ) : (
            <span>{section.empty}</span>
          )}
          <button className="button secondary" type="button" onClick={onRoleSelect}>
            {selectedRoles.length ? '권한 변경' : section.action}
          </button>
        </div>
      ) : null}
    </section>
  );
}

function FieldGrid({ fields, placeholders = {}, values, onChange }) {
  return (
    <div className="field-grid">
      {fields.map((field) => {
        const label = field.replace(' *', '');
        const required = field.includes('*');
        const large = ['내용', '업무 내용', '사용 목적'].includes(label);

        return (
          <label className={large ? 'field large' : 'field'} key={field}>
            <span>
              {label}
              {required ? <b>*</b> : null}
            </span>
            {large ? (
              <textarea value={values[label] ?? ''} onChange={(event) => onChange(label, event.target.value)} />
            ) : (
              <input
                placeholder={placeholders[label] ?? ''}
                value={values[label] ?? ''}
                onChange={(event) => onChange(label, event.target.value)}
              />
            )}
          </label>
        );
      })}
    </div>
  );
}

function UploadBox({ upload, files, onFilesChange }) {
  return (
    <div className="upload-box">
      <strong>{upload[0]}</strong>
      <span>{files.length ? files.map((file) => file.name).join(', ') : '첨부된 파일이 없습니다.'}</span>
      <label className="button secondary file-button">
        {upload[1]}
        <input
          type="file"
          multiple
          onChange={(event) => onFilesChange(Array.from(event.target.files).slice(0, 5))}
        />
      </label>
      <small>{upload[2]}</small>
    </div>
  );
}

export default FormPage;
