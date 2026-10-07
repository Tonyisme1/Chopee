package com.chopee.modules.voucher.dto;

import com.chopee.entity.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo mã giảm giá (Voucher)")
public class CreateVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Schema(description = "Mã voucher viết hoa không dấu", example = "FREESHIP_CHO")
    private String code;

    @NotNull(message = "Loại giảm giá không được để trống (PERCENTAGE hoặc FIXED_AMOUNT)")
    @Schema(description = "Loại giảm: PERCENTAGE (theo %) hoặc FIXED_AMOUNT (tiền mặt)", example = "PERCENTAGE")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    @Schema(description = "Giá trị giảm (ví dụ: 10 = 10% hoặc 20000 = 20.000 VNĐ)", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "Đơn hàng tối thiểu để áp dụng", example = "100000")
    private BigDecimal minOrderAmount;

    @Schema(description = "Số tiền giảm tối đa (khi áp dụng theo %)", example = "50000")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Số lượt sử dụng tối đa", example = "500")
    private Integer usageLimit;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Schema(description = "Thời gian bắt đầu áp dụng", example = "2026-10-01T00:00:00")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Schema(description = "Thời gian hết hạn", example = "2026-12-31T23:59:59")
    private LocalDateTime endDate;
}
