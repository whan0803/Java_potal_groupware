package reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reservation.dto.ReservationCreateRequest;
import reservation.dto.ReservationResponse;
import reservation.dto.ReservationStatusRequest;
import reservation.dto.ReservationUpdateRequest;
import reservation.dto.ResourceCreateRequest;
import reservation.dto.ResourceResponse;
import reservation.entity.Reservation;
import reservation.entity.ReservationResource;
import reservation.repository.ReservationRepository;
import reservation.repository.ReservationResourceRepository;
import user.entity.User;
import user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationResourceRepository resourceRepository;
    private final UserRepository userRepository;

    // 회의실/차량 목록 조회
    public List<ResourceResponse> getResources(
            String type
    ) {
        return resourceRepository
                .findByResourceTypeAndUseYn(type, "Y")
                .stream()
                .map(ResourceResponse::from)
                .toList();
    }

    @Transactional
    public Long createResource(
            ResourceCreateRequest request
    ) {
        String resourceName = request.resourceName().trim();
        String resourceType = request.resourceType().trim();

        if (resourceRepository.existsByResourceTypeAndResourceNameAndUseYn(
                resourceType,
                resourceName,
                "Y"
        )) {
            throw new IllegalArgumentException(
                    "이미 등록된 예약 자원입니다."
            );
        }

        ReservationResource resource = ReservationResource.create(
                resourceType,
                resourceName,
                request.resourceDescription(),
                request.capacity(),
                request.location(),
                request.vehicleNumber()
        );

        return resourceRepository.save(resource).getResourceId();
    }

    // 예약 현황 조회
    public List<ReservationResponse> getReservations(
            Long resourceId
    ) {
        return reservationRepository
                .findByResourceResourceIdAndUseYnOrderByStartDatetimeAsc(
                        resourceId,
                        "Y"
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // 예약 등록
    @Transactional
    public Long create(
            ReservationCreateRequest request
    ) {
        validatePeriod(
                request.startDateTime(),
                request.endDateTime()
        );

        ReservationResource resource =
                resolveResource(request);

        if (!"Y".equals(resource.getUseYn())) {
            throw new IllegalStateException(
                    "사용 중지된 예약 자원입니다."
            );
        }

        validateNoOverlap(
                resource.getResourceId(),
                request.startDateTime(),
                request.endDateTime(),
                null
        );

        User user =
                userRepository.findById(request.requesterId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "예약자를 찾을 수 없습니다."
                                )
                        );

        Reservation reservation =
                Reservation.create(
                        resource,
                        user,
                        request.title(),
                        request.purpose(),
                        request.startDateTime(),
                        request.endDateTime()
                );

        reservationRepository.save(reservation);

        return reservation.getReservationId();
    }

    // 상세조회
    public ReservationResponse detail(
            Long id
    ) {
        return ReservationResponse.from(
                findReservation(id)
        );
    }

    // 수정
    @Transactional
    public void update(
            Long id,
            ReservationUpdateRequest request
    ) {
        Reservation reservation =
                findReservation(id);

        validateCancelableOrUpdatable(reservation);

        validatePeriod(
                request.startDateTime(),
                request.endDateTime()
        );

        ReservationResource resource =
                request.resourceId() == null
                        && (request.resourceName() == null
                        || request.resourceName().isBlank())
                        ? reservation.getResource()
                        : resolveResource(
                                request.resourceId(),
                                request.resourceName(),
                                request.resourceType()
                        );

        validateNoOverlap(
                resource.getResourceId(),
                request.startDateTime(),
                request.endDateTime(),
                id
        );

        reservation.update(
                resource,
                request.title(),
                request.purpose(),
                request.startDateTime(),
                request.endDateTime(),
                reservation.getRequester().getUserId()
        );
    }

    // 취소
    @Transactional
    public void cancel(
            Long id
    ) {
        Reservation reservation =
                findReservation(id);

        validateCancelableOrUpdatable(reservation);

        reservation.cancel(
                reservation.getRequester().getUserId()
        );
    }

    @Transactional
    public void processStatus(
            Long id,
            ReservationStatusRequest request
    ) {
        Reservation reservation =
                findReservation(id);

        if (!List.of("APPROVED", "REJECTED").contains(request.status())) {
            throw new IllegalArgumentException(
                    "예약 상태는 APPROVED 또는 REJECTED만 가능합니다."
            );
        }

        if (!"REQUESTED".equals(reservation.getReservationStatus())) {
            throw new IllegalStateException(
                    "대기 상태의 예약만 처리할 수 있습니다."
            );
        }

        User approver =
                userRepository.findById(request.approverId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "승인자를 찾을 수 없습니다."
                                )
                        );

        reservation.process(
                request.status(),
                approver,
                request.approvalComment()
        );
    }

    private Reservation findReservation(
            Long id
    ) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약을 찾을 수 없습니다."
                        )
                );
    }

    private ReservationResource resolveResource(
            ReservationCreateRequest request
    ) {
        if (request.resourceId() != null) {
            return findResource(request.resourceId());
        }

        String resourceName =
                request.resourceName() == null
                        ? ""
                        : request.resourceName().trim();
        String resourceType =
                request.resourceType() == null
                        ? "MEETING_ROOM"
                        : request.resourceType().trim();

        if (resourceName.isBlank()) {
            throw new IllegalArgumentException(
                    "예약 자원은 필수입니다."
            );
        }

        return resourceRepository
                .findByResourceTypeAndResourceNameAndUseYn(
                        resourceType,
                        resourceName,
                        "Y"
                )
                .orElseGet(() -> resourceRepository.save(
                        ReservationResource.create(
                                resourceType,
                                resourceName,
                                "사용자 입력 자원",
                                null,
                                null,
                                null
                        )
                ));
    }

    private ReservationResource resolveResource(
            Long resourceId,
            String resourceName,
            String resourceType
    ) {
        if (resourceId != null) {
            return findResource(resourceId);
        }

        String normalizedName =
                resourceName == null ? "" : resourceName.trim();
        String normalizedType =
                resourceType == null ? "MEETING_ROOM" : resourceType.trim();

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "예약 자원은 필수입니다."
            );
        }

        return resourceRepository
                .findByResourceTypeAndResourceNameAndUseYn(
                        normalizedType,
                        normalizedName,
                        "Y"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약 자원을 찾을 수 없습니다."
                        )
                );
    }

    private ReservationResource findResource(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약 자원을 찾을 수 없습니다."
                        )
                );
    }

    private void validatePeriod(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "종료 시간은 시작 시간보다 늦어야 합니다."
            );
        }
    }

    private void validateNoOverlap(
            Long resourceId,
            LocalDateTime start,
            LocalDateTime end,
            Long reservationIdToIgnore
    ) {
        List<Reservation> reservations =
                reservationRepository.findOverlappingReservations(
                        resourceId,
                        start,
                        end
                );

        boolean overlap =
                reservations.stream()
                        .anyMatch(reservation ->
                                reservationIdToIgnore == null
                                        || !reservation
                                        .getReservationId()
                                        .equals(reservationIdToIgnore)
                        );

        if (overlap) {
            throw new IllegalArgumentException(
                    "이미 예약된 시간입니다."
            );
        }
    }

    private void validateCancelableOrUpdatable(
            Reservation reservation
    ) {
        if ("CANCELED".equals(
                reservation.getReservationStatus()
        )) {
            throw new IllegalStateException(
                    "이미 취소된 예약입니다."
            );
        }
    }
}
