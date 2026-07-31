package user.dto;

public record DuplicateCheckResponse(
        boolean available,
        String message

){

}
