package com.chopee.modules.catalog.dto;

import com.chopee.entity.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private Integer discountPercent;
    private BigDecimal stockQuantity;
    private BigDecimal soldQuantity;
    private String unit;
    private BigDecimal stepQuantity;
    private BigDecimal minOrderQuantity;
    private StorageType storageType;
    private String origin;
    private BigDecimal ratingAvg;
    private Integer reviewCount;
    private Long shopId;
    private String shopName;
    private String shopAddress;
    private Long categoryId;
    private String categoryName;
}
