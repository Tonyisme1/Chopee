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
public class ProductSearchCriteria {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private StorageType storageType;
    private String unit;
    private BigDecimal minRating;
    private Long shopId;
    private String sortBy; // newest, price_asc, price_desc, sales, rating
}
