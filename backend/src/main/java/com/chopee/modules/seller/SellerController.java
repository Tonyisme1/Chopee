package com.chopee.modules.seller;

import com.chopee.common.dto.ApiResponse;
import com.chopee.common.dto.PageResponse;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.modules.catalog.dto.ProductDetailResponse;
import com.chopee.modules.catalog.dto.ProductSummaryResponse;
import com.chopee.modules.order.dto.OrderResponse;
import com.chopee.modules.seller.dto.*;
import com.chopee.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('SELLER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Seller Center", description = "APIs dành cho Kênh Người Bán Chopee")
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/dashboard")
    @Operation(summary = "Xem thống kê tổng quan gian hàng (doanh thu, đơn hàng theo trạng thái)")
    public ResponseEntity<ApiResponse<SellerDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        SellerDashboardResponse response = sellerService.getDashboard(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products")
    @Operation(summary = "Lấy danh sách sản phẩm của gian hàng phân trang")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductSummaryResponse> response = sellerService.getProducts(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/products")
    @Operation(summary = "Thêm sản phẩm mới cho gian hàng")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProductRequest request) {
        ProductDetailResponse response = sellerService.createProduct(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm thành công", response));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Cập nhật thông tin sản phẩm của gian hàng (bảo vệ IDOR)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailResponse response = sellerService.updateProduct(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", response));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Xóa mềm sản phẩm (chuyển sang trạng thái INACTIVE)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        sellerService.deleteProduct(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    @GetMapping("/orders")
    @Operation(summary = "Lấy danh sách đơn hàng của gian hàng kèm bộ lọc trạng thái")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<OrderResponse> response = sellerService.getOrders(principal.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{orderCode}")
    @Operation(summary = "Xem chi tiết một đơn hàng của gian hàng (bảo vệ IDOR)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String orderCode) {
        OrderResponse response = sellerService.getOrderDetail(principal.getId(), orderCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/orders/{orderCode}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (CONFIRMED -> SHIPPING -> DELIVERED hoặc CANCELLED)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String orderCode,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = sellerService.updateOrderStatus(principal.getId(), orderCode, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", response));
    }
}
