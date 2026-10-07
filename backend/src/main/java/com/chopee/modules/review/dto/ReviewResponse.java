package com.chopee.modules.review.dto;

import com.chopee.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Chi tiết một đánh giá sản phẩm")
public class ReviewResponse {

    @Schema(description = "ID đánh giá", example = "1")
    private Long id;

    @Schema(description = "ID sản phẩm", example = "10")
    private Long productId;

    @Schema(description = "Tên sản phẩm", example = "Dâu Tây Đà Lạt Giống New Zealand")
    private String productName;

    @Schema(description = "ID biến thể (nếu có)")
    private Long variantId;

    @Schema(description = "Tên biến thể đã mua", example = "Hộp 500g")
    private String variantName;

    @Schema(description = "ID người đánh giá", example = "8")
    private Long userId;

    @Schema(description = "Họ tên người đánh giá", example = "Nguyễn Văn Mua")
    private String userFullName;

    @Schema(description = "Ảnh đại diện người đánh giá")
    private String userAvatarUrl;

    @Schema(description = "Số sao đánh giá (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "Nội dung nhận xét", example = "Dâu rất tươi ngọt, đóng gói cẩn thận!")
    private String comment;

    @Schema(description = "Danh sách URL ảnh / video thực tế dạng chuỗi JSON")
    private String imagesJson;

    @Schema(description = "Phản hồi từ người bán")
    private String shopReply;

    @Schema(description = "Thời gian gửi đánh giá")
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review entity) {
        if (entity == null) return null;

        Long variantId = null;
        String variantName = null;
        if (entity.getOrderItem() != null) {
            variantName = entity.getOrderItem().getVariantName();
            if (entity.getOrderItem().getVariant() != null) {
                variantId = entity.getOrderItem().getVariant().getId();
            }
        }

        return ReviewResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .variantId(variantId)
                .variantName(variantName)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userFullName(entity.getUser() != null ? entity.getUser().getFullName() : null)
                .userAvatarUrl(entity.getUser() != null ? entity.getUser().getAvatarUrl() : null)
                .rating(entity.getRating())
                .comment(entity.getComment())
                .imagesJson(entity.getImagesJson())
                .shopReply(entity.getShopReply())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
