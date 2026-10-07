package com.chopee.modules.address.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo hoặc cập nhật địa chỉ nhận hàng")
public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Schema(description = "Họ và tên người nhận", example = "Nguyễn Văn A")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phone;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Schema(description = "Tỉnh hoặc Thành phố", example = "TP. Hồ Chí Minh")
    private String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Schema(description = "Quận hoặc Huyện", example = "Quận 1")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Schema(description = "Phường hoặc Xã", example = "Phường Bến Nghé")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Schema(description = "Địa chỉ chi tiết (số nhà, tên đường, tòa nhà)", example = "123 Lê Lợi, Tòa nhà Bitexco")
    private String detailAddress;

    @Schema(description = "Đặt làm địa chỉ mặc định hay không", example = "true")
    private Boolean isDefault;
}
