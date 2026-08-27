import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';
import { api } from '../services/api.js';
import { buildRow, formTargets } from '../utils/formActions.js';
import { canUsePermission } from '../utils/permissions.js';
import RoleSelectModal from './RoleSelectModal.jsx';

const selectOptions = {
  useYn: ['사용', '미사용'],
  importantYn: ['일반', '중요'],
  attachmentYn: ['허용', '미허용'],
  scheduleType: ['PERSONAL', 'PUBLIC'],
  resourceType: ['회의실', '차량'],
  taskStatus: ['예정', '진행중', '완료', '보류'],
  allDayYn: ['아니오', '예'],
};

const dateFields = new Set(['startDate', 'endDate', 'reservationDate', 'dueDate']);
const timeFields = new Set(['startTime', 'endTime']);
const getToday = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
const getNowDatetime = () => {
  const now = new Date();
  const date = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0'),
  ].join('-');
  const time = [
    String(now.getHours()).padStart(2, '0'),
    String(now.getMinutes()).padStart(2, '0'),
  ].join(':');
  return `${date}T${time}:00`;
};
const toYn = (value, yesLabel = '예') => (value === yesLabel || value === 'Y' || value === '사용' ? 'Y' : 'N');
const normalizeField = (field) => {
  if (typeof field === 'string') {
    const label = field.replace(' *', '');
    return { name: label, label, required: field.includes('*') };
  }
  return field;
};
const getCurrentUserId = (user, lists) => {
  const numericId = Number(user?.user_id ?? user?.userId);
  if (Number.isInteger(numericId)) return numericId;
  const rowId = lists.users.rows.find((row) => row[1] === user?.id)?.[0];
  const parsedRowId = Number(rowId);
  return Number.isInteger(parsedRowId) ? parsedRowId : 1;
};
const buildDatetime = (date, time, fallbackTime) => `${date || getToday()}T${time || fallbackTime}:00`;

function FormPage({ formKey, config }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [searchParams] = useSearchParams();
  const {
    user,
    lists,
    resources,
    permissions,
    apiStatus,
    addListRow,
    updateListRow,
    addSchedule,
    addMessage,
    upsertAccount,
    saveFormRecord,
  } = useApp();
  const fields = config.fields ?? [];
  const target = formTargets[formKey];
  const editIndex = Number.parseInt(searchParams.get('index') ?? '', 10);
  const editingRow = Number.isInteger(editIndex) && target?.listKey ? lists[target.listKey]?.rows[editIndex] : null;
  const sections = useMemo(
    () => (config.sections ?? []).filter((section) => !(formKey === 'userRegister' && editingRow && section.title === '비밀번호')),
    [config.sections, editingRow, formKey],
  );
  const initialValues = useMemo(() => rowToValues(formKey, editingRow), [formKey, editingRow]);
  const initialRoles = useMemo(
    () => editingRow?._meta?.roles?.map((role) => role.roleCode ?? role.roleName).filter(Boolean) ?? [],
    [editingRow],
  );
  const [values, setValues] = useState(initialValues);
  const [selectedChip, setSelectedChip] = useState(editingRow?.[1] ?? config.chips?.[0] ?? '');
  const dynamicOptions = useMemo(() => getDynamicOptions(formKey, lists, resources, selectedChip), [formKey, lists, resources, selectedChip]);
  const [selectedRoles, setSelectedRoles] = useState(initialRoles);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState('');

  const handleChange = (name, value) => {
    setValues((current) => ({ ...current, [name]: value }));
  };

  useEffect(() => {
    let mounted = true;
    const loader = detailLoaders[formKey];
    if (!editingRow?._meta || !loader) return undefined;

    loader(editingRow._meta)
      .then((nextValues) => {
        if (mounted) setValues((current) => ({ ...current, ...nextValues }));
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, [formKey, editingRow]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const validation = validateForm(config, values, lists, { formKey, editingRow, selectedChip });
    if (!validation.ok) {
      setError(validation.message);
      return;
    }

    if (!target) return;

    const permissionAction = editingRow ? 'update' : 'create';
    if (!canUsePermission(user, permissions, pathname, permissionAction)) {
      setError(editingRow ? '수정 권한이 없습니다.' : '등록 권한이 없습니다.');
      return;
    }

    if (apiStatus.connected) {
      try {
        await saveFormRecord(formKey, values, { editingRow, selectedChip, selectedRoles, files });
        setError('');
        window.alert(config.action === '보내기' ? '발송 완료' : '저장되었습니다.');
        navigate(target.redirectTo);
      } catch (error) {
        setError(error.message || '저장 중 오류가 발생했습니다.');
      }
      return;
    }

    if (target.listKey) {
      const sourceValues = { ...values, reservationType: selectedChip };
      const rowNumber = editingRow?.[0] && /^\d+$/.test(editingRow[0])
        ? Number(editingRow[0]) - 1
        : lists[target.listKey].rows.length;
      const row = buildRow(formKey, sourceValues, rowNumber, editingRow, user);
      if (row && editingRow) updateListRow(target.listKey, editIndex, row);
      if (row && !editingRow) addListRow(target.listKey, row);
      if (formKey === 'userRegister') {
        upsertAccount({
          id: values.loginId,
          password: values.password,
          name: values.userName,
          role: selectedRoles[0] ?? '일반 사용자',
          enabled: values.useYn !== '미사용',
        });
      }
    }

    if (formKey === 'messageCompose') {
      addMessage([user?.name ?? '', '방금', values.title || '', values.receiveId || '', values.content || '']);
    }

    if (formKey === 'scheduleRegister') {
      const allDayYn = toYn(values.allDayYn);
      const userId = getCurrentUserId(user, lists);
      addSchedule({
        userId,
        title: values.title || '신규 일정',
        content: values.content || '',
        location: values.location || '',
        scheduleType: values.scheduleType || 'PERSONAL',
        startDatetime: buildDatetime(values.startDate, allDayYn === 'Y' ? '00:00' : values.startTime, '09:00'),
        endDatetime: buildDatetime(values.endDate, allDayYn === 'Y' ? '23:59' : values.endTime, '10:00'),
        allDayYn,
        useYn: values.useYn === '미사용' ? 'N' : 'Y',
        createdAt: getNowDatetime(),
        createdBy: userId,
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
      {sections.length
        ? sections.map((section) => (
            <FormSection
              section={section}
              values={values}
              onChange={handleChange}
              onRoleSelect={() => setIsRoleModalOpen(true)}
              selectedRoles={selectedRoles}
              dynamicOptions={dynamicOptions}
              key={section.title}
            />
          ))
        : null}
      {fields.length ? (
        <FieldGrid
          fields={fields}
          placeholders={config.placeholders}
          values={values}
          onChange={handleChange}
          dynamicOptions={dynamicOptions}
        />
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
      loginId: row[1],
      userName: row[2],
      department: row._meta?.department ?? row[3] ?? '',
      email: row[5],
      phone: row._meta?.phone ?? row[6] ?? '',
      useYn: row[7],
    }),
    roleRegister: () => ({
      roleCode: row[1],
      roleName: row[2],
      roleDescription: row[3],
      useYn: row[5],
    }),
    menuEdit: () => ({
      menuName: row[0],
      menuUrl: row[1],
      sortOrder: row[2],
      useYn: row[4],
    }),
    noticeRegister: () => {
      const [startDate = '', endDate = ''] = String(row[4] ?? '').split(' ~ ');
      return {
        importantYn: row[1],
        title: row[2],
        content: row._meta?.content ?? '',
        startDate,
        endDate,
      };
    },
    boardRegister: () => ({
      boardName: row[1],
      boardDescription: row[2],
      attachmentYn: row[3],
      useYn: row[4],
    }),
    postRegister: () => ({
      boardId: String(row._meta?.boardId ?? row[1]),
      title: row[2],
      content: row._meta?.content ?? '',
      useYn: row[6],
    }),
    reservationRegister: () => ({
      resourceName: row[2],
      reservationDate: row[5],
      startTime: String(row[6] ?? '').split('~')[0] ?? '',
      endTime: String(row[6] ?? '').split('~')[1] ?? '',
      purpose: row[7],
    }),
    taskRegister: () => ({
      title: row[1],
      content: row._meta?.content ?? '',
      assigneeId: row[2],
      dueDate: row[4],
      taskStatus: row[5],
    }),
    templateRegister: () => ({
      templateCode: row[1],
      templateName: row[2],
      templateDescription: row[3],
      useYn: row[4],
    }),
    codeRegister: () => ({
      codeGroupId: row[1],
      codeGroupName: row[2],
      description: row[3],
      useYn: row[4],
    }),
  };

  return mappers[formKey]?.() ?? {};
}

const detailLoaders = {
  userRegister: async (meta) => {
    const user = await api.get(`/api/users/${meta.userId}`);
    return {
      loginId: user.loginId,
      userName: user.userName,
      department: meta.department ?? '',
      email: user.email ?? '',
      phone: user.phone ?? '',
      useYn: user.useYn === 'N' ? '미사용' : '사용',
    };
  },
  noticeRegister: async (meta) => {
    const notice = await api.get(`/api/notices/${meta.noticeId}`, { increaseView: false, requireVisible: false });
    return {
      title: notice.title,
      content: notice.content ?? '',
      importantYn: notice.importantYn === 'Y' ? '중요' : '일반',
      startDate: String(notice.startDate ?? '').slice(0, 10),
      endDate: String(notice.endDate ?? '').slice(0, 10),
    };
  },
  postRegister: async (meta) => {
    const post = await api.get(`/api/posts/${meta.postId}`, { increaseView: false });
    return {
      boardId: String(post.boardId),
      title: post.title,
      content: post.content ?? '',
    };
  },
};

function validateForm(config, values, lists, options = {}) {
  const fields = [
    ...(config.fields ?? []),
    ...(config.sections?.flatMap((section) => section.fields ?? []) ?? []),
  ].map(normalizeField);
  const required = fields
    .filter((field) => field.required)
    .filter((field) => !(options.formKey === 'userRegister' && options.editingRow && field.name.includes('password')));
  const missing = required.find((field) => !String(values[field.name] ?? '').trim());
  if (missing) return { ok: false, message: `${missing.label} 항목은 필수입니다.` };

  if (values.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
    return { ok: false, message: '이메일 형식이 올바르지 않습니다.' };
  }
  if (values.phone && !/^010-\d{4}-\d{4}$/.test(values.phone)) {
    return { ok: false, message: '연락처는 010-0000-0000 형식이어야 합니다.' };
  }
  if (options.formKey === 'noticeRegister' && values.startDate && values.endDate && values.endDate < values.startDate) {
    return { ok: false, message: '게시 종료일은 시작일보다 이전일 수 없습니다.' };
  }
  if (options.formKey === 'scheduleRegister' && values.startDate && values.endDate && values.endDate < values.startDate) {
    return { ok: false, message: '종료일은 시작일보다 이전일 수 없습니다.' };
  }
  if (options.formKey === 'scheduleRegister' && values.startDate && values.endDate && values.endDate === values.startDate) {
    const isAllDay = toYn(values.allDayYn) === 'Y';
    const startTime = isAllDay ? '00:00' : values.startTime;
    const endTime = isAllDay ? '23:59' : values.endTime;
    if (startTime && endTime && endTime < startTime) {
      return { ok: false, message: '종료 시간은 시작 시간보다 이전일 수 없습니다.' };
    }
  }
  if (options.formKey === 'reservationRegister') {
    const conflict = lists.reservations.rows.some((row) => {
      if (row === options.editingRow || ['반려', '취소'].includes(row[8])) return false;
      const sameType = row[1] === options.selectedChip;
      const sameResource = row[2] === values.resourceName;
      const sameDate = row[5] === values.reservationDate;
      const [start = '', end = ''] = String(row[6] ?? '').split('~');
      return sameType && sameResource && sameDate && values.startTime < end && values.endTime > start;
    });
    if (conflict) return { ok: false, message: '이미 예약된 시간입니다.' };
  }
  if (values.loginId && lists.users.rows.some((row) => row !== options.editingRow && row[1] === values.loginId)) {
    return { ok: false, message: '이미 사용 중인 아이디입니다.' };
  }
  if (values.email && lists.users.rows.some((row) => row !== options.editingRow && row[5] === values.email)) {
    return { ok: false, message: '이미 사용 중인 이메일입니다.' };
  }
  if (values.roleCode && lists.roles.rows.some((row) => row !== options.editingRow && row[1] === values.roleCode)) {
    return { ok: false, message: '이미 사용 중인 권한 코드입니다.' };
  }
  if (options.formKey === 'menuEdit') {
    if (values.menuName && lists.menus.rows.some((row) => row !== options.editingRow && row[0] === values.menuName)) {
      return { ok: false, message: '이미 사용 중인 메뉴명입니다.' };
    }
    if (values.menuUrl && lists.menus.rows.some((row) => row !== options.editingRow && row[1] === values.menuUrl)) {
      return { ok: false, message: '이미 사용 중인 메뉴 URL입니다.' };
    }
  }
  if (values.templateCode && lists.templates.rows.some((row) => row !== options.editingRow && row[1] === values.templateCode)) {
    return { ok: false, message: '이미 사용 중인 양식 코드입니다.' };
  }
  if (values.codeGroupId && lists.codes.rows.some((row) => row !== options.editingRow && row[1] === values.codeGroupId)) {
    return { ok: false, message: '이미 사용 중인 코드 그룹 ID입니다.' };
  }

  return { ok: true };
}

function getDynamicOptions(formKey, lists, resources, selectedChip) {
  const boardOptions = lists.boards.rows
    .filter((row) => row[4] !== '미사용')
    .map((row) => ({ value: String(row._meta?.boardId ?? row[1]), label: row[1] }));
  const userOptions = lists.users.rows
    .filter((row) => row[7] !== '미사용')
    .map((row) => ({ value: String(row._meta?.userId ?? row[1]), label: `${row[2]} (${row[1]})` }));
  const resourceOptions = resources
    .filter((resource) => resource.resourceType === (selectedChip === '차량' ? 'VEHICLE' : 'MEETING_ROOM'))
    .map((resource) => ({ value: resource.resourceName, label: resource.resourceName }))
    .filter((option, index, options) => option.value && options.findIndex((item) => item.value === option.value) === index);
  return {
    boardId: boardOptions,
    receiveId: userOptions,
    assigneeId: userOptions,
    resourceName: resourceOptions,
  };
}

function FormSection({ section, values, onChange, onRoleSelect, selectedRoles = [], dynamicOptions = {} }) {
  return (
    <section className="form-section">
      <h2>{section.title}</h2>
      {section.fields ? <FieldGrid fields={section.fields} values={values} onChange={onChange} dynamicOptions={dynamicOptions} /> : null}
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

function FieldGrid({ fields, placeholders = {}, values, onChange, dynamicOptions = {} }) {
  return (
    <div className="field-grid">
      {fields.map((field) => {
        const { name, label, required = false } = normalizeField(field);
        const large = ['내용', '업무 내용', '사용 목적'].includes(label);
        const options = dynamicOptions[name] ?? selectOptions[name];

        return (
          <label className={large ? 'field large' : 'field'} key={name}>
            <span>
              {label}
              {required ? <b>*</b> : null}
            </span>
            {large ? (
              <textarea value={values[name] ?? ''} onChange={(event) => onChange(name, event.target.value)} />
            ) : options ? (
              <select
                required={required}
                value={values[name] ?? ''}
                onChange={(event) => onChange(name, event.target.value)}
              >
                <option value="">선택</option>
                {options.map((option) => (
                  <option value={typeof option === 'string' ? option : option.value} key={typeof option === 'string' ? option : option.value}>
                    {typeof option === 'string' ? option : option.label}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type={dateFields.has(name) ? 'date' : timeFields.has(name) ? 'time' : 'text'}
                placeholder={placeholders[name] ?? ''}
                required={required}
                value={values[name] ?? ''}
                onChange={(event) => onChange(name, event.target.value)}
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
