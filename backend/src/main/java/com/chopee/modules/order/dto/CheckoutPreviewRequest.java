package com.chopee.modules.order.dto;

import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewRequest {
    private List<Long> cartItemIds; // Nếu null hoặc rỗng -> tính toàn bộ giỏ hàng
    @Builder.Default
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;
    private String voucherCode;
}
