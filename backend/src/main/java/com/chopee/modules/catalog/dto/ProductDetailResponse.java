package com.chopee.modules.catalog.dto;

import com.chopee.entity.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private Integer discountPercent;
    private BigDecimal stockQuantity;
    private BigDecimal soldQuantity;
    private String unit;
    private Boolean hasVariants;
    private Integer weightGrams;
    private BigDecimal stepQuantity;
    private BigDecimal minOrderQuantity;
    private StorageType storageType;
    private String shelfLife;
    private String origin;
    private String attributes; // JSON string containing dynamic specs (VietGAP, OCOP, technical specs)
    private String tierVariation; // JSON string containing tier variations
    private BigDecimal ratingAvg;
    private Integer reviewCount;
    private Long categoryId;
    private String categoryName;
    private ShopPublicResponse shop;
    @Builder.Default
    private List<ProductImageResponse> images = new ArrayList<>();
    @Builder.Default
    private List<ProductVariantResponse> variants = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
