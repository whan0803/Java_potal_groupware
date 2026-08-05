package notice.service;

import lombok.RequiredArgsConstructor;
import notice.dto.*;
import notice.entity.Notice;
import notice.repository.NoticeRepository;
import notice.repository.NoticeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.entity.User;
import user.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // NOTICE-001, NOTICE-002, NOTICE-008
    // 목록 조회, 검색, 기간 제어, 페이징
    public Page<NoticeListResponse> getNotices(
            NoticeSearchCondition condition,
            Pageable pageable
    ) {
        Specification<Notice> specification =
                NoticeSpecification.useYnEquals("Y")
                        .and(
                                NoticeSpecification.search(
                                        condition.searchType(),
                                        condition.keyword()
                                )
                        );

        if (Boolean.TRUE.equals(condition.visibleOnly())) {
            specification = specification.and(
                    NoticeSpecification.visibleOn(LocalDate.now())
            );
        }

        Page<Notice> notices =
                noticeRepository.findAll(specification, pageable);

        return notices.map(NoticeListResponse::from);
    }

    // NOTICE-003 상세 조회 및 조회수 증가
    @Transactional
    public NoticeDetailResponse getNotice(Long noticeId) {

        Notice notice = findActiveNotice(noticeId);

        if (!notice.isVisible(LocalDate.now())) {
            throw new IllegalStateException(
                    "현재 게시 기간이 아닌 공지사항입니다."
            );
        }

        notice.increaseViewCount();

        return NoticeDetailResponse.from(notice);
    }

    // NOTICE-004 공지사항 등록
    @Transactional
    public Long createNotice(NoticeCreateRequest request) {

        validateAdmin(
                request.writerId(),
                request.admin()
        );

        validatePeriod(
                request.startDate(),
                request.endDate()
        );

        User writer = findUser(request.writerId());

        Notice notice = Notice.create(
                request.title(),
                request.content(),
                writer,
                request.startDate(),
                request.endDate(),
                request.importantYn()
        );

        return noticeRepository.save(notice).getNoticeId();
    }

    // NOTICE-005 공지사항 수정
    @Transactional
    public void updateNotice(
            Long noticeId,
            NoticeUpdateRequest request
    ) {
        validateAdmin(
                request.userId(),
                request.admin()
        );

        validatePeriod(
                request.startDate(),
                request.endDate()
        );

        Notice notice = findNotice(noticeId);

        notice.update(
                request.title(),
                request.content(),
                request.startDate(),
                request.endDate(),
                request.importantYn(),
                request.useYn(),
                request.userId()
        );
    }

    // NOTICE-006 공지사항 논리 삭제
    @Transactional
    public void deleteNotice(
            Long noticeId,
            NoticeDeleteRequest request
    ) {
        validateAdmin(
                request.userId(),
                request.admin()
        );

        Notice notice = findActiveNotice(noticeId);

        notice.delete(request.userId());
    }

    // NOTICE-007 중요 공지 설정
    @Transactional
    public void changeImportant(
            Long noticeId,
            NoticeImportantRequest request
    ) {
        validateAdmin(
                request.userId(),
                request.admin()
        );

        Notice notice = findActiveNotice(noticeId);

        notice.changeImportantYn(
                request.importantYn(),
                request.userId()
        );
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "공지사항을 찾을 수 없습니다. noticeId: "
                                        + noticeId
                        )
                );
    }

    private Notice findActiveNotice(Long noticeId) {
        Notice notice = findNotice(noticeId);

        if (!"Y".equals(notice.getUseYn())) {
            throw new IllegalStateException(
                    "삭제되었거나 사용 중지된 공지사항입니다."
            );
        }

        return notice;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다. userId: "
                                        + userId
                        )
                );
    }

    private void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "게시 종료일은 게시 시작일보다 빠를 수 없습니다."
            );
        }
    }

    private void validateAdmin(
            Long userId,
            boolean admin
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "사용자 번호는 필수입니다."
            );
        }

        if (!admin) {
            throw new IllegalStateException(
                    "관리자만 공지사항을 관리할 수 있습니다."
            );
        }
    }
}
