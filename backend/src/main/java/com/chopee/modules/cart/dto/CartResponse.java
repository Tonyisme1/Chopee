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
public class CartResponse {
    @Builder.Default
    private List<ShopCartGroupResponse> shops = new ArrayList<>();
    private int totalItemCount;
    private BigDecimal grandTotal;
}
