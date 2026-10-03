package com.chopee.entity;

import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.StorageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String thumbnailUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    // Hỗ trợ số lượng thập phân cho thực phẩm theo kg (vd: 20.5kg)
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal soldQuantity = BigDecimal.ZERO;

    // Đặc thù Chợ Thực Phẩm, Đồ Uống & Nông Sản
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String unit = "chiếc"; // "kg", "g", "bó", "khay", "lon", "thùng", "chiếc"

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minOrderQuantity = BigDecimal.ONE;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stepQuantity = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StorageType storageType = StorageType.NORMAL;

    @Column(length = 100)
    private String shelfLife; // Ví dụ: "3 ngày sau thu hoạch", "12 tháng"

    @Column(length = 150)
    private String origin;    // Ví dụ: "Đà Lạt, Lâm Đồng", "Chính hãng Philips"

    // Thuộc tính động dạng JSON (Lưu thông số kỹ thuật gia dụng hoặc chứng nhận VietGAP, OCOP)
    @Column(columnDefinition = "TEXT")
    private String attributes;

    @Column(precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal ratingAvg = new BigDecimal("5.0");

    @Column(nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

