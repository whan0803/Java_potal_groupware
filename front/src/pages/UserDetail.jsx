import Icon from '../components/Icon.jsx';

function UserDetail() {
  const fields = [
    ['이메일', 'hong@co.kr'],
    ['연락처', '010-1234-5678'],
    ['부서', 'IT기획팀'],
    ['등록일', '2024-01-15'],
  ];

  return (
    <section className="detail-card">
      <div className="profile-summary">
        <div className="profile-avatar">
          <Icon index={22} size={24.5} />
        </div>
        <div>
          <h2>홍길동</h2>
          <p className="mono">admin</p>
          <p>IT기획팀</p>
          <span className="status-badge">
            <span />
            사용
          </span>
        </div>
      </div>
      <dl className="profile-grid">
        {fields.map(([label, value]) => (
          <div className="profile-field" key={label}>
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>
      <div className="roles">
        <p>보유 권한</p>
        <div>
          <span>ROLE_ADMIN</span>
          <span>ROLE_USER</span>
        </div>
      </div>
    </section>
  );
}

export default UserDetail;
