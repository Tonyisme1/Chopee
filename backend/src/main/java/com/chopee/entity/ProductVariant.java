package com.chopee.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false, length = 100)
    private String variantName; // Ví dụ: "Túi 500g", "Thùng 24 lon", "Màu Đỏ, Size L"

    @Column(length = 100)
    private String sku; // Mã quản lý kho hàng

    @Column(columnDefinition = "TEXT")
    private String attributes; // JSON map: {"Màu sắc": "Đen", "Dung lượng": "128GB"}

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}

