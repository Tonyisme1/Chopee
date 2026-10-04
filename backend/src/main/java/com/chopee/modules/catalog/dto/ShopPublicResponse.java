package com.chopee.modules.catalog.dto;

import com.chopee.entity.enums.ShopType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPublicResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
    private String phone;
    private BigDecimal rating;
    private ShopType shopType;
    private long totalProducts;
    private LocalDateTime createdAt;
}
