package com.chopee.modules.auth;

import com.chopee.common.dto.ApiResponse;
import com.chopee.modules.auth.dto.*;
import com.chopee.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & User Management", description = "Các API đăng ký, đăng nhập và thông tin tài khoản")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản người mua (Buyer)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng username hoặc email")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để tiếp tục"));
        }
        UserProfileResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register-seller")
    @Operation(summary = "Nâng cấp tài khoản thành Người bán (Seller) và mở Shop", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<AuthResponse>> registerSeller(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RegisterSellerRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để đăng ký bán hàng"));
        }
        AuthResponse response = authService.registerSeller(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký mở gian hàng thành công", response));
    }
}
