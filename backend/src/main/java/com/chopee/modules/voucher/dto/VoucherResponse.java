package com.chopee.modules.voucher.dto;

import com.chopee.entity.Voucher;
import com.chopee.entity.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin mã giảm giá (Voucher)")
public class VoucherResponse {

    @Schema(description = "ID voucher", example = "1")
    private Long id;

    @Schema(description = "Mã voucher", example = "FREESHIP_CHO")
    private String code;

    @Schema(description = "ID shop sở hữu (null nếu voucher toàn sàn)", example = "1")
    private Long shopId;

    @Schema(description = "Tên shop sở hữu (null nếu voucher toàn sàn)", example = "Đà Lạt Farm")
    private String shopName;

    @Schema(description = "Loại giảm giá", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "Giá trị giảm", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "Giá trị đơn hàng tối thiểu", example = "100000")
    private BigDecimal minOrderAmount;

    @Schema(description = "Số tiền giảm tối đa", example = "50000")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Giới hạn số lượt dùng", example = "1000")
    private Integer usageLimit;

    @Schema(description = "Số lượt đã sử dụng", example = "42")
    private Integer usedCount;

    @Schema(description = "Ngày bắt đầu")
    private LocalDateTime startDate;

    @Schema(description = "Ngày kết thúc")
    private LocalDateTime endDate;

    @Schema(description = "Trạng thái còn hiệu lực hay không")
    private Boolean isValid;

    public static VoucherResponse fromEntity(Voucher v) {
        if (v == null) return null;
        LocalDateTime now = LocalDateTime.now();
        boolean valid = !Boolean.TRUE.equals(v.getIsDeleted())
                && (v.getStartDate() == null || !now.isBefore(v.getStartDate()))
                && (v.getEndDate() == null || !now.isAfter(v.getEndDate()))
                && (v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit());

        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .shopId(v.getShop() != null ? v.getShop().getId() : null)
                .shopName(v.getShop() != null ? v.getShop().getName() : "Toàn Sàn Chopee")
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderAmount(v.getMinOrderAmount())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .isValid(valid)
                .build();
    }
}
