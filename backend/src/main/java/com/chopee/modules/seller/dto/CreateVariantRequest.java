package com.chopee.modules.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVariantRequest {

    @NotBlank(message = "Tên phân loại không được để trống")
    private String variantName;

    private String sku;

    private String attributes;

    @NotNull(message = "Giá phân loại không được để trống")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho phân loại không được để trống")
    private BigDecimal stockQuantity;
}
