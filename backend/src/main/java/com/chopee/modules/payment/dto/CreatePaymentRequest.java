package com.chopee.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotBlank(message = "Mã gom nhóm đơn hàng (groupOrderCode) không được để trống")
    private String groupOrderCode;

    private String bankCode;
}

