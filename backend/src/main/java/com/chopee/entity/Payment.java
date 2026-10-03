package com.chopee.entity;

import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_group_code", columnList = "groupOrderCode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String groupOrderCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentGateway; // COD, VNPAY

    @Column(length = 100)
    private String transactionNo; // Mã giao dịch do VNPay trả về

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime payDate;
}

