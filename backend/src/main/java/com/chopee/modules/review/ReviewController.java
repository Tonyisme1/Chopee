package com.chopee.modules.review;

import com.chopee.common.dto.ApiResponse;
import com.chopee.common.dto.PageResponse;
import com.chopee.modules.review.dto.CreateReviewRequest;
import com.chopee.modules.review.dto.ReplyReviewRequest;
import com.chopee.modules.review.dto.ReviewResponse;
import com.chopee.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Các API đánh giá sản phẩm (FR-PROD-03), nhận xét có ảnh và phản hồi từ gian hàng")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/v1/public/products/{productId}/reviews")
    @Operation(summary = "Xem danh sách đánh giá của sản phẩm", description = "Công khai, hỗ trợ phân trang và sắp xếp mới nhất")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> response = reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/buyer/reviews")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Người mua gửi đánh giá sản phẩm đã nhận", description = "Chỉ đánh giá được khi đơn hàng đã DELIVERED/COMPLETED. Tự động tính toán lại điểm rating trung bình")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để gửi đánh giá"));
        }
        ReviewResponse response = reviewService.createReview(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đánh giá sản phẩm thành công", response));
    }

    @GetMapping("/api/v1/seller/reviews")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Người bán xem danh sách đánh giá của gian hàng mình")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getShopReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập tài khoản người bán"));
        }
        PageResponse<ReviewResponse> response = reviewService.getShopReviews(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/seller/reviews/{id}/reply")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Người bán phản hồi đánh giá của khách hàng", description = "Bảo vệ IDOR: chỉ cho phép chủ gian hàng phản hồi đánh giá trên sản phẩm của mình")
    public ResponseEntity<ApiResponse<ReviewResponse>> replyReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đánh giá") @PathVariable Long id,
            @Valid @RequestBody ReplyReviewRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập tài khoản người bán"));
        }
        ReviewResponse response = reviewService.replyReview(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi đánh giá thành công", response));
    }
}
