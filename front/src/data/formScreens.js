export const formScreens = {
  userRegister: {
    sections: [
      { title: '기본 정보', fields: ['아이디 *', '이름 *', '이메일 *', '부서', '연락처', '사용여부'] },
      { title: '비밀번호', fields: ['비밀번호 *', '비밀번호 확인 *'] },
      { title: '권한 설정', empty: '부여된 권한이 없습니다.', action: '권한 선택' },
    ],
  },
  roleRegister: {
    width: 'narrow',
    fields: ['권한 코드 *', '권한명 *', '설명', '사용여부'],
    help: '영문 대문자, 숫자, 언더스코어(_)만 입력',
  },
  menuEdit: {
    width: 'narrow',
    fields: ['상위 메뉴', '메뉴명 *', 'URL', '정렬 순서', '사용여부'],
    placeholders: { URL: '/example/path', '정렬 순서': '1' },
  },
  noticeRegister: {
    fields: ['제목 *', '내용 *', '게시 시작일', '게시 종료일', '중요 공지 여부'],
    upload: ['첨부파일', '파일 선택', '최대 10MB, 5개까지 첨부 가능'],
  },
  boardRegister: {
    width: 'medium',
    fields: ['게시판명 *', '설명', '첨부파일 허용 여부', '사용여부'],
  },
  postRegister: {
    fields: ['게시판 ID *', '제목 *', '내용 *'],
    upload: ['첨부파일', '파일 선택', '게시판별 첨부 허용 여부와 파일 크기를 검증합니다'],
  },
  reservationRegister: {
    width: 'medium',
    chips: ['회의실', '차량'],
    subtitle: '예약 정보 입력',
    fields: ['자원 선택 *', '예약일 *', '시작 시간 *', '종료 시간 *', '사용 목적'],
    action: '중복 확인',
  },
  resourceRegister: {
    width: 'medium',
    fields: ['자원 유형 *', '자원명 *', '설명', '수용/탑승 인원', '위치', '차량 번호'],
  },
  taskRegister: {
    width: 'medium',
    fields: ['업무 제목 *', '업무 내용 *', '담당자 *', '마감일 *', '상태'],
    upload: ['첨부파일', '파일 추가', 'PDF·Office·이미지·ZIP, 파일당 최대 10MB'],
  },
  templateRegister: {
    width: 'medium',
    fields: ['양식 코드 *', '양식명 *', '설명', '기본 내용', '사용여부'],
  },
  scheduleRegister: {
    width: 'medium',
    fields: ['일정 유형 *', '제목 *', '내용', '장소', '시작일 *', '시작 시간', '종료일 *', '종료 시간', '종일 일정 여부', '사용여부'],
  },
  messageCompose: {
    width: 'medium',
    fields: ['수신자 *', '제목 *', '내용 *'],
    action: '보내기',
  },
  codeRegister: {
    width: 'medium',
    fields: ['코드 그룹 ID *', '그룹명 *', '설명', '사용여부'],
  },
};
