package com.chopee.modules.cart.dto;

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
public class ShopCartGroupResponse {
    private Long shopId;
    private String shopName;
    private String shopSlug;
    private String shopLogoUrl;
    private String shopAddress;
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal shopSubtotal;
}
