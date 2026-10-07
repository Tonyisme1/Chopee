package com.chopee.modules.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu đánh giá sản phẩm sau khi nhận hàng")
public class CreateReviewRequest {

    @NotNull(message = "ID mục đơn hàng không được để trống")
    @Schema(description = "ID mục đơn hàng (order_item_id) đã giao thành công", example = "1")
    private Long orderItemId;

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    @Schema(description = "Số sao đánh giá (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "Nội dung nhận xét chi tiết", example = "Sản phẩm tươi ngon, đóng gói cẩn thận, giao hàng rất nhanh!")
    private String comment;

    @Schema(description = "Danh sách URL ảnh hoặc video đánh giá dạng chuỗi JSON", example = "[\"https://example.com/img1.jpg\"]")
    private String imagesJson;
}
