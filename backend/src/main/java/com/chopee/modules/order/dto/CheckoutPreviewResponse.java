package com.chopee.modules.order.dto;

import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.ShippingMethod;
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
public class CheckoutPreviewResponse {
    @Builder.Default
    private List<ShopCheckoutPreview> shops = new ArrayList<>();
    private ShippingMethod shippingMethod;
    private PaymentMethod paymentMethod;
    private BigDecimal totalItemsAmount;
    private BigDecimal totalShippingFee;
    private BigDecimal totalDiscountAmount;
    private BigDecimal grandFinalAmount;
}
