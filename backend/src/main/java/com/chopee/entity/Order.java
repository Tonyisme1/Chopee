package com.chopee.entity;

import com.chopee.entity.enums.CancelledBy;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.PaymentStatus;
import com.chopee.entity.enums.ShippingMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_group_code", columnList = "groupOrderCode"),
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_shop_id", columnList = "shop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderCode;

    // Mã gom nhóm đơn hàng trong cùng một lượt thanh toán từ nhiều shop khác nhau
    @Column(nullable = false, length = 50)
    private String groupOrderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, length = 100)
    private String shippingName;

    @Column(nullable = false, length = 20)
    private String shippingPhone;

    @Column(nullable = false, length = 255)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Phân định rõ nguồn giảm giá: Shop tự chịu vs Sàn Chopee trợ giá (Phục vụ đối soát Payout)
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shopDiscountAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformDiscountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CancelledBy cancelledBy;

    @Column(length = 255)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

