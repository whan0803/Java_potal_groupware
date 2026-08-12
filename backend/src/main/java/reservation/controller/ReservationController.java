package reservation.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reservation.dto.*;
import reservation.service.ReservationService;


import java.util.List;


@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {



    private final ReservationService service;



    // 회의실 목록
    // /api/reservations/resources?type=MEETING_ROOM

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceResponse>> resources(
            @RequestParam String type
    ){

        return ResponseEntity.ok(
                service.getResources(type)
        );

    }

    @PostMapping("/resources")
    public ResponseEntity<Long> createResource(
            @Valid @RequestBody ResourceCreateRequest request
    ) {
        return ResponseEntity.ok(
                service.createResource(request)
        );
    }



    // 예약현황

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> list(
            @RequestParam Long resourceId
    ){

        return ResponseEntity.ok(
                service.getReservations(resourceId)
        );

    }




    // 예약 등록

    @PostMapping
    public ResponseEntity<Long> create(
            @Valid @RequestBody ReservationCreateRequest request
    ){

        return ResponseEntity.ok(
                service.create(request)
        );

    }





    // 상세조회

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> detail(
            @PathVariable Long id
    ){

        return ResponseEntity.ok(
                service.detail(id)
        );

    }





    // 수정

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ){

        service.update(id,request);

        return ResponseEntity.ok().build();

    }





    // 취소

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id
    ){

        service.cancel(id);

        return ResponseEntity.ok().build();

    }


    // 승인 / 반려

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> processStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusRequest request
    ){

        service.processStatus(id, request);

        return ResponseEntity.ok().build();

    }


}
