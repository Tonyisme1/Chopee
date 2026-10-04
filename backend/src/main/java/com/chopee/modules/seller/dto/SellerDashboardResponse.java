package com.chopee.modules.seller.dto;

import com.chopee.entity.enums.ShopStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDashboardResponse {
    private Long shopId;
    private String shopName;
    private ShopStatus shopStatus;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long shippingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal ratingAvg;
}
