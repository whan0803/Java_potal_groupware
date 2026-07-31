const today = '2026-07-28';

export const formTargets = {
  userRegister: { listKey: 'users', redirectTo: '/users' },
  roleRegister: { listKey: 'roles', redirectTo: '/roles' },
  menuEdit: { listKey: 'menus', redirectTo: '/menus' },
  noticeRegister: { listKey: 'notices', redirectTo: '/notices' },
  boardRegister: { listKey: 'boards', redirectTo: '/boards' },
  postRegister: { listKey: 'posts', redirectTo: '/posts' },
  reservationRegister: { listKey: 'reservations', redirectTo: '/reservations' },
  templateRegister: { listKey: 'templates', redirectTo: '/templates' },
  taskRegister: { listKey: 'tasks', redirectTo: '/tasks' },
  scheduleRegister: { redirectTo: '/schedule' },
  messageCompose: { redirectTo: '/messages' },
  codeRegister: { listKey: 'codes', redirectTo: '/codes' },
};

export function buildRow(formKey, values, rowCount) {
  const number = String(rowCount + 1);
  const get = (label, fallback) => values[label]?.trim() || fallback;

  const builders = {
    userRegister: () => [
      number,
      get('아이디', `user${number}`),
      get('이름', '신규 사용자'),
      get('부서', '미지정'),
      get('이메일', `user${number}@co.kr`),
      '0회',
      get('사용여부', '사용'),
      today,
      '보기',
    ],
    roleRegister: () => [
      number,
      get('권한 코드', `ROLE_CUSTOM_${number}`),
      get('권한명', '신규 권한'),
      get('설명', '사용자 정의 권한'),
      '0명',
      get('사용여부', '사용'),
      today,
      '수정',
    ],
    menuEdit: () => [
      get('메뉴명', '신규 메뉴'),
      get('URL', `/custom/${number}`),
      get('정렬 순서', number),
      '1단계',
      get('사용여부', '사용'),
      '수정',
    ],
    noticeRegister: () => [
      number,
      get('중요 공지 여부', '일반'),
      get('제목', '신규 공지사항'),
      '홍길동',
      `${get('게시 시작일', today)} ~ ${get('게시 종료일', today)}`,
      '0',
      today,
      '수정',
    ],
    boardRegister: () => [
      number,
      get('게시판명', '신규 게시판'),
      get('설명', '게시판 설명'),
      get('첨부파일 허용 여부', '허용'),
      get('사용여부', '사용'),
      '수정',
    ],
    postRegister: () => [
      number,
      get('게시판 ID', '공지 게시판'),
      get('제목', '신규 게시글'),
      '홍길동',
      '0',
      today,
      '사용',
      '상세',
    ],
    reservationRegister: () => [
      number,
      get('예약 유형', '회의실'),
      get('자원 선택', '신규 자원'),
      '홍길동',
      'IT기획팀',
      get('예약일', today),
      `${get('시작 시간', '09:00')}~${get('종료 시간', '10:00')}`,
      get('사용 목적', '예약 목적'),
      '대기',
      '상세',
    ],
    taskRegister: () => [
      number,
      get('업무 제목', '신규 업무'),
      get('담당자', '홍길동'),
      'IT기획팀',
      get('마감일', today),
      '예정',
      '0%',
      '-',
      '수정',
    ],
    templateRegister: () => [
      number,
      get('양식 코드', `TMP_CUSTOM_${number}`),
      get('양식명', '신규 문서양식'),
      get('설명', '사용자 정의 결재 양식'),
      get('사용여부', '사용'),
      '수정',
    ],
    codeRegister: () => [
      number,
      get('코드 그룹 ID', `CUSTOM_CODE_${number}`),
      get('그룹명', '신규 코드 그룹'),
      get('설명', '공통코드 설명'),
      get('사용여부', '사용'),
      '상세',
    ],
  };

  return builders[formKey]?.();
}
