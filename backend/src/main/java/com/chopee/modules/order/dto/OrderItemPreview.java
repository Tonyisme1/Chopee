package com.chopee.modules.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPreview {
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal subtotal;
}
