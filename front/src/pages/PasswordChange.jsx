import { useState } from 'react';
import { useApp } from '../context/AppContext.jsx';

function PasswordChange() {
  const { changePassword } = useApp();
  const [values, setValues] = useState({ currentPassword: '', nextPassword: '', confirmPassword: '' });
  const [message, setMessage] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    const result = await changePassword(values);
    setMessage(result.message);
    if (result.ok) setValues({ currentPassword: '', nextPassword: '', confirmPassword: '' });
  };

  return (
    <form className="content-card form-card narrow" onSubmit={handleSubmit}>
      <div className="field-grid">
        <label className="field">
          <span>기존 비밀번호</span>
          <input
            type="password"
            value={values.currentPassword}
            onChange={(event) => setValues((current) => ({ ...current, currentPassword: event.target.value }))}
          />
        </label>
        <label className="field">
          <span>새 비밀번호</span>
          <input
            type="password"
            value={values.nextPassword}
            onChange={(event) => setValues((current) => ({ ...current, nextPassword: event.target.value }))}
          />
        </label>
        <label className="field">
          <span>비밀번호 확인</span>
          <input
            type="password"
            value={values.confirmPassword}
            onChange={(event) => setValues((current) => ({ ...current, confirmPassword: event.target.value }))}
          />
        </label>
      </div>
      {message ? <p className={message.includes('변경') ? 'form-success' : 'form-error'}>{message}</p> : null}
      <div className="form-actions">
        <button className="button primary" type="submit">
          변경
        </button>
      </div>
    </form>
  );
}

export default PasswordChange;
