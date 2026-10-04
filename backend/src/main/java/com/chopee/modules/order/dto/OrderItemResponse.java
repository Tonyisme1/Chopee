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
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String thumbnailUrl;
    private Long variantId;
    private String variantName;
    private String unit;
    private BigDecimal productPrice;
    private BigDecimal quantity;
    private BigDecimal subtotal;
}
