package com.chopee.modules.order.dto;

import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResultResponse {
    private String groupOrderCode;
    private int totalOrdersCreated;
    private BigDecimal grandTotal;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    @Builder.Default
    private List<OrderResponse> orders = new ArrayList<>();
}
