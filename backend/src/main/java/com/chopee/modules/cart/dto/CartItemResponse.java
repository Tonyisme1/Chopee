package com.chopee.modules.cart.dto;

import com.chopee.entity.enums.StorageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String thumbnailUrl;
    private Long variantId;
    private String variantName;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal itemSubtotal;
    private String unit;
    private BigDecimal stepQuantity;
    private BigDecimal minOrderQuantity;
    private BigDecimal stockQuantity;
    private StorageType storageType;
    private boolean available;
    private Long shopId;
    private String shopName;
    private String shopSlug;

    @JsonProperty("sellingPrice")
    public BigDecimal getSellingPrice() {
        return unitPrice;
    }

    @JsonProperty("subtotal")
    public BigDecimal getSubtotal() {
        return itemSubtotal;
    }
}
