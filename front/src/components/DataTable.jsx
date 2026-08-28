const statusValues = ['사용', '미사용', '승인', '대기', '반려', '진행중', '완료', '예정', '보류'];

function renderCell(cell, options = {}) {
  if (options.isPostAction) {
    return (
      <span className="role-action-group">
        <button className="table-action" type="button" onClick={() => options.onAction('상세')}>
          상세
        </button>
        {options.canUpdate ? (
          <button className="table-action" type="button" onClick={() => options.onAction('수정')}>
            수정
          </button>
        ) : null}
        {options.canDelete ? (
          <button className="table-action danger" type="button" onClick={() => options.onAction('삭제')}>
            삭제
          </button>
        ) : null}
      </span>
    );
  }

  if (options.isTaskAction) {
    return (
      <span className="role-action-group">
        {options.canUpdate ? (
          <>
            <button className="table-action" type="button" onClick={() => options.onAction('수정')}>
              수정
            </button>
            <button className="table-action" type="button" onClick={() => options.onAction('진행률')}>
              진행률
            </button>
            <button className="table-action" type="button" onClick={() => options.onAction('상태')}>
              상태
            </button>
          </>
        ) : (
          <span className="muted-cell">읽기 전용</span>
        )}
        {options.canDelete ? (
          <button className="table-action danger" type="button" onClick={() => options.onAction('삭제')}>
            삭제
          </button>
        ) : null}
      </span>
    );
  }

  if (options.isReservationAction) {
    return (
      <span className="role-action-group">
        {options.canUpdate ? (
          <button className="table-action" type="button" onClick={() => options.onAction('수정')}>
            수정
          </button>
        ) : null}
        {options.canDelete ? (
          <button className="table-action danger" type="button" onClick={() => options.onAction('취소')}>
            취소
          </button>
        ) : null}
      </span>
    );
  }

  if (options.isReservationApprovalAction) {
    if (cell === '승인 / 반려' && options.canUpdate) {
      return (
        <span className="role-action-group">
          <button className="table-action" type="button" onClick={() => options.onAction('승인')}>
            승인
          </button>
          <button className="table-action danger" type="button" onClick={() => options.onAction('반려')}>
            반려
          </button>
        </span>
      );
    }

    return <span className="muted-cell">처리 완료</span>;
  }

  if (options.isApprovalAction) {
    if (cell === '결재 처리' && options.canUpdate) {
      return (
        <span className="role-action-group">
          <button className="table-action" type="button" onClick={() => options.onAction('승인')}>
            승인
          </button>
          <button className="table-action danger" type="button" onClick={() => options.onAction('반려')}>
            반려
          </button>
        </span>
      );
    }

    return <span className="muted-cell">{cell === '결재 처리' ? '처리 권한 없음' : '처리 완료'}</span>;
  }

  if (options.isRoleAction) {
    return (
      <span className="role-action-group">
        <button className="table-action" type="button" onClick={() => options.onAction('보기')}>
          보기
        </button>
        {options.canUpdate ? (
          <button className="table-action" type="button" onClick={() => options.onAction('수정')}>
            수정
          </button>
        ) : null}
      </span>
    );
  }

  if (options.isAction) {
    const primaryAction = options.canUpdate || ['보기', '상세', '처리 완료'].includes(cell);
    if (options.canDelete) {
      return (
        <span className="role-action-group">
          {primaryAction ? (
            <button className="table-action" type="button" onClick={options.onAction}>
              {cell}
            </button>
          ) : null}
          <button className="table-action danger" type="button" onClick={() => options.onAction('삭제')}>
            삭제
          </button>
        </span>
      );
    }

    return primaryAction ? (
      <button className="table-action" type="button" onClick={options.onAction}>
        {cell}
      </button>
    ) : (
      <span className="muted-cell">읽기 전용</span>
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

function DataTable({ columns, rows, onAction, listKey, canUpdate = true, canDelete = true, canEditRow }) {
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
                    isPostAction: listKey === 'posts' && cellIndex === actionColumnIndex,
                    isTaskAction: listKey === 'tasks' && cellIndex === actionColumnIndex,
                    isReservationAction: listKey === 'reservations' && cellIndex === actionColumnIndex,
                    isReservationApprovalAction: listKey === 'reservationApproval' && cellIndex === actionColumnIndex,
                    isApprovalAction: listKey === 'approval' && cellIndex === actionColumnIndex,
                    isRoleAction: listKey === 'roles' && cellIndex === actionColumnIndex,
                    isAction: cellIndex === actionColumnIndex,
                    canUpdate: canUpdate && (canEditRow?.(row) ?? true),
                    canDelete: canDelete && (canEditRow?.(row) ?? true) && canDeleteRow(listKey, cell),
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

function canDeleteRow(listKey, action) {
  if (!['수정', '상세', '보기'].includes(action)) return false;
  return !['logs', 'approval'].includes(listKey);
}

export default DataTable;
