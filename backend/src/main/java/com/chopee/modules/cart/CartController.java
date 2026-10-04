package com.chopee.modules.cart;

import com.chopee.common.dto.ApiResponse;
import com.chopee.modules.cart.dto.AddToCartRequest;
import com.chopee.modules.cart.dto.CartItemResponse;
import com.chopee.modules.cart.dto.CartResponse;
import com.chopee.modules.cart.dto.UpdateCartItemRequest;
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
@RequestMapping("/api/v1/buyer/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
@Tag(name = "Cart Management", description = "Các API giỏ hàng người mua: gom nhóm theo shop, hỗ trợ số lượng lẻ (0.5kg thịt, rau) và kiểm tra tồn kho")
@SecurityRequirement(name = "BearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng hiện tại", description = "Trả về giỏ hàng gom nhóm theo từng Shop bán hàng, tính tổng tiền từng shop và tổng giá trị đơn hàng")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem giỏ hàng"));
        }
        CartResponse response = cartService.getCart(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng", description = "Hỗ trợ số lượng lẻ theo bước nhảy stepQuantity (0.5kg rau, thịt) và biến thể SKU")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thao tác giỏ hàng"));
        }
        CartItemResponse response = cartService.addToCart(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm vào giỏ hàng thành công", response));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng", description = "Kiểm tra tồn kho và ràng buộc bước nhảy số lượng")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID mục giỏ hàng (cart_item id)") @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thao tác giỏ hàng"));
        }
        CartItemResponse response = cartService.updateCartItem(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng thành công", response));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Xóa một sản phẩm khỏi giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID mục giỏ hàng cần xóa") @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thao tác giỏ hàng"));
        }
        cartService.removeCartItem(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", null));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Xóa toàn bộ giỏ hàng của người mua")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thao tác giỏ hàng"));
        }
        cartService.clearCart(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã làm trống giỏ hàng", null));
    }
}
