package com.chopee.modules.order.dto;

import com.chopee.entity.enums.OrderStatus;
import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.PaymentStatus;
import com.chopee.entity.enums.ShippingMethod;
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
public class OrderResponse {
    private Long id;
    private String orderCode;
    private String groupOrderCode;
    private Long shopId;
    private String shopName;
    private String shopSlug;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private ShippingMethod shippingMethod;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal shopDiscountAmount;
    private BigDecimal platformDiscountAmount;
    private BigDecimal finalAmount;
    private String note;
    private String cancelledBy;
    private String cancellationReason;
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
