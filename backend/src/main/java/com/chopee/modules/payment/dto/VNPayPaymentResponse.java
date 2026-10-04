package com.chopee.modules.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayPaymentResponse {
    private String paymentUrl;
    private String groupOrderCode;
    private BigDecimal amount;
    private String message;
}

