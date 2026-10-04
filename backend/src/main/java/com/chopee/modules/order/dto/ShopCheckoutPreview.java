package com.chopee.modules.order.dto;

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
public class ShopCheckoutPreview {
    private Long shopId;
    private String shopName;
    @Builder.Default
    private List<OrderItemPreview> items = new ArrayList<>();
    private BigDecimal shopItemsTotal;
    private BigDecimal shopShippingFee;
    private BigDecimal shopDiscount;
    private BigDecimal shopFinalTotal;
}
