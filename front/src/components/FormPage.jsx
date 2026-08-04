import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { buildRow, formTargets } from '../utils/formActions.js';
import RoleSelectModal from './RoleSelectModal.jsx';

const selectOptions = {
  사용여부: ['사용', '미사용'],
  '중요 공지 여부': ['일반', '중요'],
  '첨부파일 허용 여부': ['허용', '미허용'],
};

const dateFields = new Set(['게시 시작일', '게시 종료일', '예약일', '마감일', '시작일', '종료일']);
const timeFields = new Set(['시작 시간', '종료 시간']);
const getToday = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

function FormPage({ formKey, config }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user, lists, addListRow, updateListRow, addSchedule, addMessage, upsertAccount } = useApp();
  const fields = config.fields ?? [];
  const target = formTargets[formKey];
  const editIndex = Number.parseInt(searchParams.get('index') ?? '', 10);
  const editingRow = Number.isInteger(editIndex) && target?.listKey ? lists[target.listKey]?.rows[editIndex] : null;
  const initialValues = useMemo(() => rowToValues(formKey, editingRow), [formKey, editingRow]);
  const [values, setValues] = useState(initialValues);
  const [selectedChip, setSelectedChip] = useState(editingRow?.[1] ?? config.chips?.[0] ?? '');
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState('');

  const handleChange = (label, value) => {
    setValues((current) => ({ ...current, [label]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const validation = validateForm(config, values, lists, { formKey, editingRow, selectedChip });
    if (!validation.ok) {
      setError(validation.message);
      return;
    }

    if (!target) return;

    if (target.listKey) {
      const sourceValues = { ...values, '예약 유형': selectedChip };
      const rowNumber = editingRow?.[0] && /^\d+$/.test(editingRow[0])
        ? Number(editingRow[0]) - 1
        : lists[target.listKey].rows.length;
      const row = buildRow(formKey, sourceValues, rowNumber, editingRow);
      if (row && editingRow) updateListRow(target.listKey, editIndex, row);
      if (row && !editingRow) addListRow(target.listKey, row);
      if (formKey === 'userRegister') {
        upsertAccount({
          id: values.아이디,
          password: editingRow ? values.비밀번호 : values.비밀번호 || 'user123',
          name: values.이름,
          role: selectedRoles[0] ?? '일반 사용자',
          enabled: values.사용여부 !== '미사용',
        });
      }
    }

    if (formKey === 'messageCompose') {
      addMessage([user?.name ?? '홍길동', '방금', values.제목 || '새 쪽지', values.수신자 || '수신자', values.내용 || '']);
    }

    if (formKey === 'scheduleRegister') {
      addSchedule({
        title: values.제목 || '신규 일정',
        date: values.시작일 || getToday(),
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
          <button
            className="button secondary"
            type="button"
            onClick={() => {
              const validation = validateForm(config, values, lists, { formKey, editingRow, selectedChip });
              setError(validation.ok ? '' : validation.message);
              window.alert(validation.ok ? '예약 가능한 시간입니다.' : validation.message);
            }}
          >
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

function rowToValues(formKey, row) {
  if (!row) return {};

  const mappers = {
    userRegister: () => ({
      아이디: row[1],
      이름: row[2],
      부서: row[3],
      이메일: row[4],
      사용여부: row[6],
    }),
    roleRegister: () => ({
      '권한 코드': row[1],
      권한명: row[2],
      설명: row[3],
      사용여부: row[5],
    }),
    menuEdit: () => ({
      메뉴명: row[0],
      URL: row[1],
      '정렬 순서': row[2],
      사용여부: row[4],
    }),
    noticeRegister: () => {
      const [startDate = '', endDate = ''] = String(row[4] ?? '').split(' ~ ');
      return {
        '중요 공지 여부': row[1],
        제목: row[2],
        '게시 시작일': startDate,
        '게시 종료일': endDate,
      };
    },
    boardRegister: () => ({
      게시판명: row[1],
      설명: row[2],
      '첨부파일 허용 여부': row[3],
      사용여부: row[4],
    }),
    postRegister: () => ({
      '게시판 ID': row[1],
      제목: row[2],
      사용여부: row[6],
    }),
    reservationRegister: () => ({
      '자원 선택': row[2],
      예약일: row[5],
      '시작 시간': String(row[6] ?? '').split('~')[0] ?? '',
      '종료 시간': String(row[6] ?? '').split('~')[1] ?? '',
      '사용 목적': row[7],
    }),
    taskRegister: () => ({
      '업무 제목': row[1],
      담당자: row[2],
      마감일: row[4],
    }),
    templateRegister: () => ({
      '양식 코드': row[1],
      양식명: row[2],
      설명: row[3],
      사용여부: row[4],
    }),
    codeRegister: () => ({
      '코드 그룹 ID': row[1],
      그룹명: row[2],
      설명: row[3],
      사용여부: row[4],
    }),
  };

  return mappers[formKey]?.() ?? {};
}

function validateForm(config, values, lists, options = {}) {
  const fields = [
    ...(config.fields ?? []),
    ...(config.sections?.flatMap((section) => section.fields ?? []) ?? []),
  ];
  const required = fields
    .filter((field) => field.includes('*'))
    .map((field) => field.replace(' *', ''))
    .filter((field) => !(options.formKey === 'userRegister' && options.editingRow && field.includes('비밀번호')));
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
  if (options.formKey === 'reservationRegister') {
    const conflict = lists.reservations.rows.some((row) => {
      if (row === options.editingRow || row[8] === '반려') return false;
      const sameType = row[1] === options.selectedChip;
      const sameResource = row[2] === values['자원 선택'];
      const sameDate = row[5] === values.예약일;
      const [start = '', end = ''] = String(row[6] ?? '').split('~');
      return sameType && sameResource && sameDate && values['시작 시간'] < end && values['종료 시간'] > start;
    });
    if (conflict) return { ok: false, message: '이미 예약된 시간입니다.' };
  }
  if (values.아이디 && lists.users.rows.some((row) => row !== options.editingRow && row[1] === values.아이디)) {
    return { ok: false, message: '이미 사용 중인 아이디입니다.' };
  }
  if (values.이메일 && lists.users.rows.some((row) => row !== options.editingRow && row[4] === values.이메일)) {
    return { ok: false, message: '이미 사용 중인 이메일입니다.' };
  }
  if (values['권한 코드'] && lists.roles.rows.some((row) => row !== options.editingRow && row[1] === values['권한 코드'])) {
    return { ok: false, message: '이미 사용 중인 권한 코드입니다.' };
  }
  if (values['양식 코드'] && lists.templates.rows.some((row) => row !== options.editingRow && row[1] === values['양식 코드'])) {
    return { ok: false, message: '이미 사용 중인 양식 코드입니다.' };
  }
  if (values['코드 그룹 ID'] && lists.codes.rows.some((row) => row !== options.editingRow && row[1] === values['코드 그룹 ID'])) {
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
        const options = selectOptions[label];

        return (
          <label className={large ? 'field large' : 'field'} key={field}>
            <span>
              {label}
              {required ? <b>*</b> : null}
            </span>
            {large ? (
              <textarea value={values[label] ?? ''} onChange={(event) => onChange(label, event.target.value)} />
            ) : options ? (
              <select
                required={required}
                value={values[label] ?? ''}
                onChange={(event) => onChange(label, event.target.value)}
              >
                <option value="">선택</option>
                {options.map((option) => (
                  <option value={option} key={option}>
                    {option}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type={dateFields.has(label) ? 'date' : timeFields.has(label) ? 'time' : 'text'}
                placeholder={placeholders[label] ?? ''}
                required={required}
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
