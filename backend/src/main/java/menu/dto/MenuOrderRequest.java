package menu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record MenuOrderRequest(
        @NotEmpty(message = "순서를 변경할 메뉴가 필요합니다")
        List<@Valid MenuOrderItem> menus,

        @NotNull(message = "처리자 번호는 필수입니다")
        Long userId
) {

    public record MenuOrderItem(
            @NotNull(message = "메뉴 번호는 필수입니다.")
            Long menuId,


            @NotNull(message = "정렬 순서는 필수입니다.")
            @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
            Integer sortOrder
    ){}
}
