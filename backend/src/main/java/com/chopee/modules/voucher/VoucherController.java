package com.chopee.modules.voucher;

import com.chopee.common.dto.ApiResponse;
import com.chopee.modules.voucher.dto.CreateVoucherRequest;
import com.chopee.modules.voucher.dto.ValidateVoucherResponse;
import com.chopee.modules.voucher.dto.VoucherResponse;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Voucher & Promotion", description = "Các API mã giảm giá Chopee toàn sàn & mã riêng của từng gian hàng (FR-PAY-03)")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/api/v1/public/vouchers")
    @Operation(summary = "Lấy danh sách mã giảm giá toàn sàn Chopee đang có hiệu lực")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getPlatformVouchers() {
        List<VoucherResponse> response = voucherService.getPlatformVouchers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/public/shops/{shopId}/vouchers")
    @Operation(summary = "Lấy danh sách mã giảm giá riêng của một gian hàng")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getShopVouchers(
            @Parameter(description = "ID gian hàng") @PathVariable Long shopId) {
        List<VoucherResponse> response = voucherService.getShopVouchers(shopId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/public/vouchers/validate")
    @Operation(summary = "Kiểm tra tính hợp lệ của mã giảm giá và tính số tiền giảm", description = "Kiểm tra hạn sử dụng, điều kiện đơn tối thiểu và tính mức giảm tương ứng")
    public ResponseEntity<ApiResponse<ValidateVoucherResponse>> validateVoucher(
            @Parameter(description = "Mã voucher") @RequestParam String code,
            @Parameter(description = "ID shop (tùy chọn)") @RequestParam(required = false) Long shopId,
            @Parameter(description = "Tổng giá trị đơn hàng") @RequestParam(defaultValue = "0") BigDecimal orderAmount) {
        ValidateVoucherResponse response = voucherService.validateVoucher(code, shopId, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/seller/vouchers")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Người bán xem danh sách voucher của gian hàng mình")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getSellerVouchers(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập tài khoản người bán"));
        }
        List<VoucherResponse> response = voucherService.getSellerVouchers(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/seller/vouchers")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Người bán tạo mã giảm giá riêng cho gian hàng")
    public ResponseEntity<ApiResponse<VoucherResponse>> createShopVoucher(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateVoucherRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập tài khoản người bán"));
        }
        VoucherResponse response = voucherService.createShopVoucher(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo mã giảm giá gian hàng thành công", response));
    }

    @PostMapping("/api/v1/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Quản trị viên tạo mã giảm giá toàn sàn Chopee")
    public ResponseEntity<ApiResponse<VoucherResponse>> createPlatformVoucher(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateVoucherRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập tài khoản Admin"));
        }
        VoucherResponse response = voucherService.createPlatformVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo mã giảm giá toàn sàn thành công", response));
    }
}
