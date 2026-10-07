package com.chopee.modules.voucher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kết quả kiểm tra tính hợp lệ và số tiền giảm giá của Voucher")
public class ValidateVoucherResponse {

    @Schema(description = "Mã voucher", example = "FREESHIP_CHO")
    private String code;

    @Schema(description = "Hợp lệ hay không", example = "true")
    private Boolean isValid;

    @Schema(description = "Lý do nếu không hợp lệ", example = "Đơn hàng chưa đạt giá trị tối thiểu 100.000 VNĐ")
    private String message;

    @Schema(description = "Số tiền được giảm", example = "20000")
    private BigDecimal discountAmount;

    @Schema(description = "Tổng tiền sau khi đã trừ giảm giá", example = "180000")
    private BigDecimal finalAmount;
}
