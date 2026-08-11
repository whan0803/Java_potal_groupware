package reservation.dto;

import reservation.entity.ReservationResource;

public record ResourceResponse(
        Long resourceId,

        String resourceType,

        String resourceName,

        Integer capacity,
        String location,
        String vehicleNumber
) {
    public static  ResourceResponse from(
            ReservationResource resource
    ){
        return new ResourceResponse(
                resource.getResourceId(),

                resource.getResourceType(),

                resource.getResourceName(),

                resource.getCapacity(),

                resource.getLocation(),

                resource.getVehicleNumber()
        );
    }
}
