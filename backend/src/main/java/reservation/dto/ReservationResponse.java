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

        ,

        String statusName


){

    public static ReservationResponse from(
            Reservation reservation
    ){
        return from(reservation, reservation.getReservationStatus());
    }

    public static ReservationResponse from(
            Reservation reservation,
            String statusName
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

                reservation.getReservationStatus(),

                statusName

        );

    }

}
