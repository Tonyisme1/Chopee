package com.chopee.modules.auth.dto;

import com.chopee.entity.enums.ShopType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSellerRequest {

    @NotBlank(message = "Tên shop không được để trống")
    private String shopName;

    private String shopDescription;

    @NotBlank(message = "Địa chỉ shop không được để trống")
    private String shopAddress;

    @NotBlank(message = "Số điện thoại shop không được để trống")
    private String shopPhone;

    private String shopLogoUrl;

    private String shopBannerUrl;

    @Builder.Default
    private ShopType shopType = ShopType.GENERAL;
}
