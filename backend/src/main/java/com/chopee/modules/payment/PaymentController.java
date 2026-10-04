package com.chopee.modules.payment;

import com.chopee.common.dto.ApiResponse;
import com.chopee.modules.payment.dto.CreatePaymentRequest;
import com.chopee.modules.payment.dto.VNPayIpnResponse;
import com.chopee.modules.payment.dto.VNPayPaymentResponse;
import com.chopee.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Tag(name = "Payment Gateway", description = "Cổng thanh toán trực tuyến VNPay Sandbox & COD")
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Khởi tạo giao dịch thanh toán VNPay Sandbox")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    @PostMapping("/vnpay/create-payment")
    public ApiResponse<VNPayPaymentResponse> createPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để thanh toán");
        }
        String clientIp = getClientIp(httpRequest);
        VNPayPaymentResponse response = paymentService.createVNPayPayment(principal.getId(), request, clientIp);
        return ApiResponse.success("Khởi tạo thanh toán VNPay thành công", response);
    }

    @Operation(summary = "IPN Webhook nhận kết quả thanh toán từ VNPay Server")
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VNPayIpnResponse> receiveIpn(@RequestParam Map<String, String> allParams) {
        VNPayIpnResponse ipnResponse = paymentService.processIpn(allParams);
        return ResponseEntity.ok(ipnResponse);
    }

    @Operation(summary = "Callback nhận phản hồi khi người dùng quay lại từ cổng VNPay")
    @GetMapping("/vnpay/callback")
    public ApiResponse<Map<String, Object>> receiveCallback(@RequestParam Map<String, String> allParams) {
        Map<String, Object> result = paymentService.processCallback(allParams);
        return ApiResponse.success("Xử lý kết quả thanh toán VNPay", result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

