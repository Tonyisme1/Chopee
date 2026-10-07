package com.chopee.modules.address.dto;

import com.chopee.entity.UserAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin địa chỉ nhận hàng")
public class AddressResponse {

    @Schema(description = "ID địa chỉ", example = "1")
    private Long id;

    @Schema(description = "Họ và tên người nhận", example = "Nguyễn Văn A")
    private String receiverName;

    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phone;

    @Schema(description = "Tỉnh hoặc Thành phố", example = "TP. Hồ Chí Minh")
    private String province;

    @Schema(description = "Quận hoặc Huyện", example = "Quận 1")
    private String district;

    @Schema(description = "Phường hoặc Xã", example = "Phường Bến Nghé")
    private String ward;

    @Schema(description = "Địa chỉ chi tiết", example = "123 Lê Lợi")
    private String detailAddress;

    @Schema(description = "Địa chỉ đầy đủ ghép chuỗi", example = "123 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh")
    private String fullAddress;

    @Schema(description = "Là địa chỉ mặc định hay không", example = "true")
    private Boolean isDefault;

    public static AddressResponse fromEntity(UserAddress entity) {
        if (entity == null) return null;
        String full = String.format("%s, %s, %s, %s",
                entity.getDetailAddress(),
                entity.getWard(),
                entity.getDistrict(),
                entity.getProvince());

        return AddressResponse.builder()
                .id(entity.getId())
                .receiverName(entity.getReceiverName())
                .phone(entity.getPhone())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .detailAddress(entity.getDetailAddress())
                .fullAddress(full)
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .build();
    }
}
