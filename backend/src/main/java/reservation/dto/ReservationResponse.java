package reservation.dto;


import reservation.entity.Reservation;


import java.time.LocalDateTime;


public record ReservationResponse(

        Long reservationId,

        Long resourceId,

        String resourceName,

        Long requesterId,

        String requesterName,

        String title,

        String purpose,

        LocalDateTime startDatetime,

        LocalDateTime endDatetime,

        String status


){

    public static ReservationResponse from(
            Reservation reservation
    ){

        return new ReservationResponse(

                reservation.getReservationId(),

                reservation.getResource().getResourceId(),

                reservation.getResource().getResourceName(),

                reservation.getRequester().getUserId(),

                reservation.getRequester().getUserName(),

                reservation.getTitle(),

                reservation.getPurpose(),

                reservation.getStartDatetime(),

                reservation.getEndDatetime(),

                reservation.getReservationStatus()

        );

    }

}