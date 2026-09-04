import { emptyCodeDetail, normalizeField, useFormPage } from '../hooks/useFormPage.js';
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

function FormPage({ formKey, config }) {
  const {
    fields,
    sections,
    values,
    selectedChip,
    selectedRoles,
    isRoleModalOpen,
    files,
    error,
    dynamicOptions,
    commonCodeOptions,
    handleChange,
    handleSubmit,
    handleCancel,
    handleDuplicateCheck,
    setSelectedChip,
    setFiles,
    openRoleModal,
    closeRoleModal,
    confirmRoles,
  } = useFormPage(formKey, config);

  return (
    <form className={`content-card form-card ${config.width ?? ''}`} onSubmit={handleSubmit}>
      {config.chips ? <ChoiceRow chips={config.chips} selectedChip={selectedChip} onChange={setSelectedChip} /> : null}
      {config.subtitle ? <h2 className="section-heading">{config.subtitle}</h2> : null}
      {formKey === 'userRegister' ? <SelectedRoleStrip selectedRoles={selectedRoles} /> : null}
      {sections.map((section) => (
        <FormSection
          section={section}
          values={values}
          onChange={handleChange}
          onRoleSelect={openRoleModal}
          selectedRoles={selectedRoles}
          dynamicOptions={dynamicOptions}
          commonCodeOptions={commonCodeOptions}
          key={section.title}
        />
      ))}
      {fields.length ? (
        <FieldGrid
          fields={fields}
          placeholders={config.placeholders}
          values={values}
          onChange={handleChange}
          dynamicOptions={dynamicOptions}
          commonCodeOptions={commonCodeOptions}
        />
      ) : null}
      {config.detailEditor ? <CommonCodeDetailEditor details={values.details ?? [emptyCodeDetail]} onChange={(details) => handleChange('details', details)} /> : null}
      {config.help ? <p className="form-help">{config.help}</p> : null}
      {config.upload ? <UploadBox upload={config.upload} files={files} onFilesChange={setFiles} /> : null}
      {error ? <p className="form-error">{error}</p> : null}
      <FormActions action={config.action} onCancel={handleCancel} onDuplicateCheck={handleDuplicateCheck} />
      {isRoleModalOpen ? (
        <RoleSelectModal
          selectedRoles={selectedRoles}
          onClose={closeRoleModal}
          onConfirm={confirmRoles}
        />
      ) : null}
    </form>
  );
}

function ChoiceRow({ chips, selectedChip, onChange }) {
  return (
    <div className="choice-row">
      {chips.map((chip) => (
        <button
          className={selectedChip === chip ? 'active' : ''}
          type="button"
          key={chip}
          onClick={() => onChange(chip)}
        >
          {chip}
        </button>
      ))}
    </div>
  );
}

function SelectedRoleStrip({ selectedRoles }) {
  return (
    <div className="selected-role-strip">
      <strong>선택된 권한</strong>
      <span>{selectedRoles.length ? selectedRoles.join(', ') : '선택된 권한이 없습니다.'}</span>
    </div>
  );
}

function FormSection({ section, values, onChange, onRoleSelect, selectedRoles = [], dynamicOptions = {}, commonCodeOptions = {} }) {
  return (
    <section className="form-section">
      <h2>{section.title}</h2>
      {section.fields ? <FieldGrid fields={section.fields} values={values} onChange={onChange} dynamicOptions={dynamicOptions} commonCodeOptions={commonCodeOptions} /> : null}
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

function FieldGrid({ fields, placeholders = {}, values, onChange, dynamicOptions = {}, commonCodeOptions = {} }) {
  return (
    <div className="field-grid">
      {fields.map((field) => {
        const { name, label, required = false, commonCodeGroup, emptyLabel = '선택' } = normalizeField(field);
        const large = ['내용', '업무 내용', '사용 목적'].includes(label);
        const options = commonCodeOptions[commonCodeGroup]?.length
          ? commonCodeOptions[commonCodeGroup]
          : dynamicOptions[name] ?? selectOptions[name];

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
                <option value="">{emptyLabel}</option>
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

function CommonCodeDetailEditor({ details, onChange }) {
  const rows = details.length ? details : [emptyCodeDetail];
  const updateDetail = (index, name, value) => {
    onChange(rows.map((detail, detailIndex) => (detailIndex === index ? { ...detail, [name]: value } : detail)));
  };
  const addDetail = () => {
    onChange([...rows, { ...emptyCodeDetail, sortOrder: String(rows.length + 1) }]);
  };
  const removeDetail = (index) => {
    const nextRows = rows.filter((_, detailIndex) => detailIndex !== index);
    onChange(nextRows.length ? nextRows : [emptyCodeDetail]);
  };

  return (
    <section className="form-section code-detail-editor">
      <div className="section-title-row">
        <h2>상세 코드</h2>
        <button className="button secondary" type="button" onClick={addDetail}>
          행 추가
        </button>
      </div>
      <div className="code-detail-table">
        <div className="code-detail-head">
          <span>코드값</span>
          <span>표시명</span>
          <span>순서</span>
          <span>사용</span>
          <span>삭제</span>
        </div>
        {rows.map((detail, index) => (
          <div className="code-detail-row" key={index}>
            <input
              value={detail.codeValue ?? ''}
              placeholder="READY"
              onChange={(event) => updateDetail(index, 'codeValue', event.target.value)}
            />
            <input
              value={detail.codeName ?? ''}
              placeholder="예정"
              onChange={(event) => updateDetail(index, 'codeName', event.target.value)}
            />
            <input
              value={detail.sortOrder ?? ''}
              inputMode="numeric"
              placeholder={String(index + 1)}
              onChange={(event) => updateDetail(index, 'sortOrder', event.target.value)}
            />
            <select value={detail.useYn ?? '사용'} onChange={(event) => updateDetail(index, 'useYn', event.target.value)}>
              <option value="사용">사용</option>
              <option value="미사용">미사용</option>
            </select>
            <button className="button secondary" type="button" onClick={() => removeDetail(index)}>
              삭제
            </button>
          </div>
        ))}
      </div>
    </section>
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

function FormActions({ action, onCancel, onDuplicateCheck }) {
  return (
    <div className="form-actions">
      <button className="button secondary" type="button" onClick={onCancel}>
        취소
      </button>
      {action === '중복 확인' ? (
        <button className="button secondary" type="button" onClick={onDuplicateCheck}>
          중복 확인
        </button>
      ) : null}
      <button className="button primary" type="submit">
        {action === '보내기' ? '보내기' : '저장'}
      </button>
    </div>
  );
}

export default FormPage;
