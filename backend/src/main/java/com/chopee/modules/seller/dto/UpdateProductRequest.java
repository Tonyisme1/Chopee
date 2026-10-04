package com.chopee.modules.seller.dto;

import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    private String thumbnailUrl;

    private BigDecimal originalPrice;

    @NotNull(message = "Giá bán không được để trống")
    private BigDecimal sellingPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    private BigDecimal stockQuantity;

    private String unit;

    private BigDecimal minOrderQuantity;

    private BigDecimal stepQuantity;

    private StorageType storageType;

    private String shelfLife;

    private String origin;

    private String attributes;

    private ProductStatus status;

    private List<String> imageUrls;

    private List<CreateVariantRequest> variants;
}
