package com.chopee.modules.admin;

import com.chopee.common.dto.ApiResponse;
import com.chopee.common.dto.PageResponse;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.modules.admin.dto.AdminDashboardResponse;
import com.chopee.modules.admin.dto.UpdateShopStatusRequest;
import com.chopee.modules.catalog.dto.ShopPublicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Management", description = "APIs dành cho Quản trị viên Sàn Chopee")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Xem bảng điều khiển tổng quan toàn sàn (GMV, người dùng, gian hàng, đơn hàng)")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse response = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/shops")
    @Operation(summary = "Lấy danh sách các gian hàng phân trang kèm bộ lọc trạng thái")
    public ResponseEntity<ApiResponse<PageResponse<ShopPublicResponse>>> getShops(
            @RequestParam(required = false) ShopStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ShopPublicResponse> response = adminService.getShops(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/shops/{id}/status")
    @Operation(summary = "Phê duyệt, từ chối hoặc khóa gian hàng vi phạm chính sách")
    public ResponseEntity<ApiResponse<ShopPublicResponse>> updateShopStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShopStatusRequest request) {
        ShopPublicResponse response = adminService.updateShopStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái gian hàng thành công", response));
    }
}
