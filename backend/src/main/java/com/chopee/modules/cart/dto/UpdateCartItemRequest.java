package com.chopee.modules.cart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng đặt mua phải lớn hơn 0")
    private BigDecimal quantity;
}
