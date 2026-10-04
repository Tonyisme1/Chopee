package com.chopee.modules.admin.dto;

import com.chopee.entity.enums.ShopStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShopStatusRequest {

    @NotNull(message = "Trạng thái gian hàng không được để trống")
    private ShopStatus status;

    private String reason;
}
