package com.chopee.modules.admin.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalSellers;
    private long totalBuyers;
    private long totalShops;
    private long pendingShops;
    private long approvedShops;
    private long lockedShops;
    private long totalOrders;
    private BigDecimal totalGmv;
}
