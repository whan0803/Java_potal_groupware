package reservation.dto;

public record ReservationStatusRequest(
        String status,
        Long approverId,
        String approvalComment
) {
}
