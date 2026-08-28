export const formScreens = {
  userRegister: {
    sections: [
      {
        title: '기본 정보',
        fields: [
          { name: 'loginId', label: '아이디', required: true },
          { name: 'userName', label: '이름', required: true },
          { name: 'email', label: '이메일', required: true },
          { name: 'department', label: '부서' },
          { name: 'phone', label: '연락처' },
          { name: 'useYn', label: '사용여부' },
        ],
      },
      {
        title: '비밀번호',
        fields: [
          { name: 'password', label: '비밀번호', required: true },
          { name: 'passwordConfirm', label: '비밀번호 확인', required: true },
        ],
      },
      { title: '권한 설정', empty: '부여된 권한이 없습니다.', action: '권한 선택' },
    ],
  },
  roleRegister: {
    width: 'narrow',
    fields: [
      { name: 'roleCode', label: '권한 코드', required: true },
      { name: 'roleName', label: '권한명', required: true },
      { name: 'roleDescription', label: '설명' },
      { name: 'useYn', label: '사용여부' },
    ],
    help: '영문 대문자, 숫자, 언더스코어(_)만 입력',
  },
  menuEdit: {
    width: 'narrow',
    fields: [
      { name: 'parentMenuName', label: '상위 메뉴' },
      { name: 'menuName', label: '메뉴명', required: true },
      { name: 'menuUrl', label: 'URL' },
      { name: 'sortOrder', label: '정렬 순서' },
      { name: 'useYn', label: '사용여부' },
    ],
    placeholders: { menuUrl: '/example/path', sortOrder: '1' },
  },
  noticeRegister: {
    fields: [
      { name: 'title', label: '제목', required: true },
      { name: 'content', label: '내용', required: true },
      { name: 'startDate', label: '게시 시작일' },
      { name: 'endDate', label: '게시 종료일' },
      { name: 'importantYn', label: '중요 공지 여부' },
    ],
    upload: ['첨부파일', '파일 선택', '최대 10MB, 5개까지 첨부 가능'],
  },
  boardRegister: {
    width: 'medium',
    fields: [
      { name: 'boardName', label: '게시판명', required: true },
      { name: 'boardDescription', label: '설명' },
      { name: 'attachmentYn', label: '첨부파일 허용 여부' },
      { name: 'useYn', label: '사용여부' },
    ],
  },
  postRegister: {
    fields: [
      { name: 'boardId', label: '게시판 ID', required: true },
      { name: 'title', label: '제목', required: true },
      { name: 'content', label: '내용', required: true },
    ],
    upload: ['첨부파일', '파일 선택', '게시판별 첨부 허용 여부와 파일 크기를 검증합니다'],
  },
  reservationRegister: {
    width: 'medium',
    chips: ['회의실', '차량'],
    subtitle: '예약 정보 입력',
    fields: [
      { name: 'resourceName', label: '자원 선택', required: true },
      { name: 'reservationDate', label: '예약일', required: true },
      { name: 'startTime', label: '시작 시간', required: true },
      { name: 'endTime', label: '종료 시간', required: true },
      { name: 'purpose', label: '사용 목적' },
    ],
    action: '중복 확인',
  },
  resourceRegister: {
    width: 'medium',
    fields: [
      { name: 'resourceType', label: '자원 유형', required: true, commonCodeGroup: 'RESOURCE_TYPE' },
      { name: 'resourceName', label: '자원명', required: true },
      { name: 'resourceDescription', label: '설명' },
      { name: 'capacity', label: '수용/탑승 인원' },
      { name: 'location', label: '위치' },
      { name: 'vehicleNumber', label: '차량 번호' },
    ],
  },
  taskRegister: {
    width: 'medium',
    fields: [
      { name: 'title', label: '업무 제목', required: true },
      { name: 'content', label: '업무 내용', required: true },
      { name: 'assigneeId', label: '담당자', required: true },
      { name: 'dueDate', label: '마감일', required: true },
      { name: 'taskStatus', label: '상태', commonCodeGroup: 'TASK_STATUS' },
      { name: 'priority', label: '우선순위', commonCodeGroup: 'TASK_PRIORITY' },
    ],
    upload: ['첨부파일', '파일 추가', 'PDF·Office·이미지·ZIP, 파일당 최대 10MB'],
  },
  templateRegister: {
    width: 'medium',
    fields: [
      { name: 'templateCode', label: '양식 코드', required: true },
      { name: 'templateName', label: '양식명', required: true },
      { name: 'templateDescription', label: '설명' },
      { name: 'templateContent', label: '기본 내용' },
      { name: 'useYn', label: '사용여부' },
    ],
  },
  scheduleRegister: {
    width: 'medium',
    fields: [
      { name: 'scheduleType', label: '일정 유형', required: true, commonCodeGroup: 'SCHEDULE_TYPE' },
      { name: 'title', label: '제목', required: true },
      { name: 'content', label: '내용' },
      { name: 'location', label: '장소' },
      { name: 'startDate', label: '시작일', required: true },
      { name: 'startTime', label: '시작 시간' },
      { name: 'endDate', label: '종료일', required: true },
      { name: 'endTime', label: '종료 시간' },
      { name: 'allDayYn', label: '종일 일정 여부' },
      { name: 'useYn', label: '사용여부' },
    ],
  },
  messageCompose: {
    width: 'medium',
    fields: [
      { name: 'receiveId', label: '수신자', required: true },
      { name: 'title', label: '제목', required: true },
      { name: 'content', label: '내용', required: true },
    ],
    action: '보내기',
  },
  codeRegister: {
    width: 'medium',
    fields: [
      { name: 'codeGroupId', label: '코드 그룹 ID', required: true },
      { name: 'codeGroupName', label: '그룹명', required: true },
      { name: 'description', label: '설명' },
      { name: 'useYn', label: '사용여부' },
    ],
    detailEditor: true,
  },
};
