package com.chopee.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String variantName;
    private String sku;
    private String attributes;
    private BigDecimal price;
    private BigDecimal stockQuantity;
}
