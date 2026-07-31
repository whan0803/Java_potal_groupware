const statusValues = ['사용', '미사용', '승인', '대기', '반려', '진행중', '완료', '예정', '보류'];

function renderCell(cell, options = {}) {
  if (options.isRoleAction) {
    return (
      <span className="role-action-group">
        <button className="table-action" type="button" onClick={() => options.onAction('보기')}>
          보기
        </button>
        <button className="table-action" type="button" onClick={() => options.onAction('수정')}>
          수정
        </button>
      </span>
    );
  }

  if (options.isAction) {
    return (
      <button className="table-action" type="button" onClick={options.onAction}>
        {cell}
      </button>
    );
  }

  if (statusValues.includes(cell)) return <span className={`pill ${cell}`}>{cell}</span>;

  if (String(cell).endsWith('%')) {
    const value = Number.parseInt(cell, 10);

    return (
      <span className="progress-cell">
        <span style={{ width: `${value}%` }} />
        {cell}
      </span>
    );
  }

  return cell;
}

function DataTable({ columns, rows, onAction, listKey }) {
  const actionColumnIndex = columns.findIndex((column) => ['관리', '처리'].includes(column));

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column}>{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={`${row[0]}-${rowIndex}`}>
              {row.map((cell, cellIndex) => (
                <td key={`${cell}-${cellIndex}`}>
                  {renderCell(cell, {
                    isRoleAction: listKey === 'roles' && cellIndex === actionColumnIndex,
                    isAction: cellIndex === actionColumnIndex,
                    onAction: (actionValue = cell) => onAction?.(row, actionValue),
                  })}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default DataTable;
