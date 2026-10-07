package com.chopee.modules.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu phản hồi đánh giá từ người bán")
public class ReplyReviewRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Schema(description = "Phản hồi của chủ shop gửi khách hàng", example = "Cảm ơn bạn đã tin tưởng ủng hộ shop! Chúc bạn và gia đình ngon miệng ạ!")
    private String shopReply;
}
