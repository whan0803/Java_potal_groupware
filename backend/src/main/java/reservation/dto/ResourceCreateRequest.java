package reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResourceCreateRequest(
        @NotBlank(message = "자원 유형은 필수입니다.")
        @Pattern(regexp = "MEETING_ROOM|VEHICLE", message = "자원 유형은 MEETING_ROOM 또는 VEHICLE이어야 합니다.")
        String resourceType,

        @NotBlank(message = "자원명은 필수입니다.")
        @Size(max = 100, message = "자원명은 100자 이하여야 합니다.")
        String resourceName,

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        String resourceDescription,

        Integer capacity,

        @Size(max = 255, message = "위치는 255자 이하여야 합니다.")
        String location,

        @Size(max = 30, message = "차량 번호는 30자 이하여야 합니다.")
        String vehicleNumber
) {
}
