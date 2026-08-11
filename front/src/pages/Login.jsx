import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext.jsx';

function Login() {
  const { user, login } = useApp();
  const navigate = useNavigate();
  const [values, setValues] = useState({ id: '', password: '' });
  const [error, setError] = useState('');

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (event) => {
    event.preventDefault();
    const result = await login(values);
    if (!result.ok) {
      setError(result.message);
      return;
    }
    navigate('/', { replace: true });
  };

  return (
    <main className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <div>
          <strong>기반 포털 관리시스템</strong>
          <span>Groupware Administration Portal</span>
        </div>
        <label>
          아이디
          <input
            value={values.id}
            onChange={(event) => setValues((current) => ({ ...current, id: event.target.value }))}
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={values.password}
            onChange={(event) => setValues((current) => ({ ...current, password: event.target.value }))}
          />
        </label>
        <button className="button primary" type="submit">
          로그인
        </button>
        {error ? <p className="form-error">{error}</p> : null}
      </form>
    </main>
  );
}

export default Login;
