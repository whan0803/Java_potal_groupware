const iconPath = (index) => `/assets/figma-icons/icon-${String(index).padStart(2, '0')}.svg`;

function Icon({ index, size = 14 }) {
  return (
    <span className="icon" style={{ '--icon-size': `${size}px` }} aria-hidden="true">
      <img src={iconPath(index)} alt="" />
    </span>
  );
}

export default Icon;
