package com.chopee.modules.order;

import com.chopee.common.dto.ApiResponse;
import com.chopee.common.dto.PageResponse;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.modules.order.dto.*;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
@Tag(name = "Order & Checkout", description = "Các API thanh toán, tách đơn hàng đa shop (Multi-Vendor Order Splitting) và theo dõi đơn hàng")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout-preview")
    @Operation(summary = "Xem trước thông tin thanh toán (Checkout Preview)", description = "Tính toán chi tiết phí vận chuyển từng gian hàng (Standard 15k, Express 25k), giảm giá và tổng tiền")
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> previewCheckout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) CheckoutPreviewRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để tiếp tục"));
        }
        CheckoutPreviewRequest safeRequest = request != null ? request : new CheckoutPreviewRequest();
        CheckoutPreviewResponse response = orderService.previewCheckout(principal.getId(), safeRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Tiến hành Đặt hàng (Checkout)", description = "Tự động phân tách đơn hàng theo từng Shop với mã groupOrderCode chung, trừ tồn kho nguyên tử chống overselling")
    public ResponseEntity<ApiResponse<CheckoutResultResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để đặt hàng"));
        }
        CheckoutResultResponse result = orderService.createOrder(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công", result));
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn hàng của người mua", description = "Xem lịch sử mua sắm với phân trang và lọc theo trạng thái (PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getBuyerOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Lọc theo trạng thái đơn hàng") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem đơn hàng"));
        }
        PageResponse<OrderResponse> response = orderService.getBuyerOrders(principal.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Xem chi tiết một đơn hàng theo mã đơn")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng riêng lẻ (VD: ORD-20261004-123456)") @PathVariable String orderCode) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem đơn hàng"));
        }
        OrderResponse response = orderService.getOrderByCode(principal.getId(), orderCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/group/{groupOrderCode}")
    @Operation(summary = "Xem danh sách các đơn hàng thuộc cùng một đợt thanh toán đa shop")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByGroupCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã gom nhóm thanh toán (VD: GRP-179109...)") @PathVariable String groupOrderCode) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem đơn hàng"));
        }
        List<OrderResponse> response = orderService.getOrdersByGroupCode(principal.getId(), groupOrderCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{orderCode}/cancel")
    @Operation(summary = "Hủy đơn hàng (Khi đang chờ xác nhận PENDING)", description = "Tự động hoàn lại số lượng tồn kho nguyên tử cho sản phẩm")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng cần hủy") @PathVariable String orderCode,
            @RequestParam(required = false) String reason) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thao tác đơn hàng"));
        }
        OrderResponse response = orderService.cancelOrder(principal.getId(), orderCode, reason);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", response));
    }
}
