package com.chopee.modules.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    private int totalItemCount;
    private BigDecimal grandTotal;

    @JsonProperty("totalAmount")
    public BigDecimal getTotalAmount() {
        return grandTotal;
    }
}
