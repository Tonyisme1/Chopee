package com.chopee.modules.payment;

import com.chopee.entity.Order;
import com.chopee.entity.Payment;
import com.chopee.entity.enums.PaymentMethod;
import com.chopee.entity.enums.PaymentStatus;
import com.chopee.modules.payment.dto.CreatePaymentRequest;
import com.chopee.modules.payment.dto.VNPayIpnResponse;
import com.chopee.modules.payment.dto.VNPayPaymentResponse;
import com.chopee.repository.OrderRepository;
import com.chopee.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public VNPayPaymentResponse createVNPayPayment(Long userId, CreatePaymentRequest request, String ipAddress) {
        String groupOrderCode = request.getGroupOrderCode();
        List<Order> orders = orderRepository.findByGroupOrderCodeAndUserId(groupOrderCode, userId);

        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy đơn hàng cần thanh toán hoặc bạn không có quyền thanh toán đơn hàng này");
        }

        boolean allPaid = orders.stream().allMatch(o -> o.getPaymentStatus() == PaymentStatus.PAID);
        if (allPaid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đơn hàng này đã được thanh toán hoàn tất trước đó");
        }

        // Tính tổng tiền toàn bộ các đơn hàng con trong nhóm
        BigDecimal grandTotal = orders.stream()
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // VNPay yêu cầu số tiền nhân 100 (đơn vị: VNĐ * 100)
        long amountInVndX100 = grandTotal.multiply(new BigDecimal(100)).longValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String createDate = now.format(formatter);
        String expireDate = now.plusMinutes(15).format(formatter);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInVndX100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", groupOrderCode);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang Chopee " + groupOrderCode);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            vnpParams.put("vnp_BankCode", request.getBankCode().trim());
        }

        String queryUrl = VNPayHelper.buildQueryUrl(vnpParams, vnPayConfig.getHashSecret());
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + queryUrl;

        // Lưu bản ghi giao dịch ở trạng thái UNPAID
        Payment payment = paymentRepository.findByGroupOrderCode(groupOrderCode).stream()
                .findFirst()
                .orElse(Payment.builder()
                        .groupOrderCode(groupOrderCode)
                        .paymentGateway(PaymentMethod.VNPAY)
                        .amount(grandTotal)
                        .status(PaymentStatus.UNPAID)
                        .build());
        payment.setAmount(grandTotal);
        payment.setStatus(PaymentStatus.UNPAID);
        paymentRepository.save(payment);

        return VNPayPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .groupOrderCode(groupOrderCode)
                .amount(grandTotal)
                .message("Tạo URL thanh toán VNPay Sandbox thành công")
                .build();
    }

    /**
     * Xử lý IPN Webhook từ cổng VNPay (Server-to-Server)
     */
    @Transactional
    public VNPayIpnResponse processIpn(Map<String, String> params) {
        log.info("Nhận IPN từ VNPay: {}", params);

        // 1. Kiểm tra tính hợp lệ của chữ ký số (Checksum)
        boolean isSignatureValid = VNPayHelper.verifySignature(params, vnPayConfig.getHashSecret());
        if (!isSignatureValid) {
            log.warn("Chữ ký IPN VNPay không hợp lệ");
            return VNPayIpnResponse.builder().rspCode("97").message("Invalid Checksum").build();
        }

        // 2. Kiểm tra sự tồn tại của đơn hàng
        String groupOrderCode = params.get("vnp_TxnRef");
        List<Order> orders = orderRepository.findByGroupOrderCode(groupOrderCode);
        if (orders.isEmpty()) {
            log.warn("Không tìm thấy đơn hàng tương ứng mã groupOrderCode: {}", groupOrderCode);
            return VNPayIpnResponse.builder().rspCode("01").message("Order not Found").build();
        }

        // 3. Kiểm tra số tiền thanh toán
        BigDecimal totalOrderAmount = orders.stream()
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedAmountX100 = totalOrderAmount.multiply(new BigDecimal(100));

        String vnpAmount = params.get("vnp_Amount");
        if (vnpAmount == null || expectedAmountX100.compareTo(new BigDecimal(vnpAmount)) != 0) {
            log.warn("Số tiền IPN không khớp. Kỳ vọng: {}, Thực tế VNPay gửi: {}", expectedAmountX100, vnpAmount);
            return VNPayIpnResponse.builder().rspCode("04").message("Invalid Amount").build();
        }

        // 4. Kiểm tra trạng thái đơn hàng (tránh cập nhật trùng lặp)
        boolean alreadyPaid = orders.stream().allMatch(o -> o.getPaymentStatus() == PaymentStatus.PAID);
        if (alreadyPaid) {
            log.info("Đơn hàng {} đã được xác nhận thanh toán trước đó", groupOrderCode);
            return VNPayIpnResponse.builder().rspCode("02").message("Order already confirmed").build();
        }

        // 5. Cập nhật trạng thái thanh toán
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        Payment payment = paymentRepository.findByGroupOrderCode(groupOrderCode).stream()
                .findFirst()
                .orElse(Payment.builder()
                        .groupOrderCode(groupOrderCode)
                        .paymentGateway(PaymentMethod.VNPAY)
                        .amount(totalOrderAmount)
                        .build());

        if ("00".equals(vnpResponseCode)) {
            // Thanh toán thành công: Cập nhật toàn bộ đơn con sang PAID
            for (Order order : orders) {
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
            }
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionNo(transactionNo);
            paymentRepository.save(payment);

            log.info("Thanh toán thành công cho đơn nhóm: {}, Mã giao dịch VNPay: {}", groupOrderCode, transactionNo);
            return VNPayIpnResponse.builder().rspCode("00").message("Confirm Success").build();
        } else {
            // Giao dịch không thành công -> giữ trạng thái UNPAID để khách hàng có thể thanh toán lại
            payment.setStatus(PaymentStatus.UNPAID);
            payment.setTransactionNo(transactionNo);
            paymentRepository.save(payment);

            log.warn("Giao dịch VNPay thất bại với mã lỗi: {}", vnpResponseCode);
            return VNPayIpnResponse.builder().rspCode("00").message("Confirm Success").build();
        }
    }

    /**
     * Xử lý Callback chuyển hướng từ trình duyệt sau khi khách hàng thanh toán tại cổng VNPay
     */
    @Transactional
    public Map<String, Object> processCallback(Map<String, String> params) {
        boolean isSignatureValid = VNPayHelper.verifySignature(params, vnPayConfig.getHashSecret());
        String groupOrderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        Map<String, Object> response = new HashMap<>();
        response.put("groupOrderCode", groupOrderCode);
        response.put("transactionNo", transactionNo);
        response.put("responseCode", responseCode);

        if (!isSignatureValid) {
            response.put("status", "INVALID_SIGNATURE");
            response.put("message", "Chữ ký bảo mật không hợp lệ!");
            return response;
        }

        if ("00".equals(responseCode)) {
            // Cập nhật trạng thái nếu IPN chưa kịp tới
            List<Order> orders = orderRepository.findByGroupOrderCode(groupOrderCode);
            for (Order order : orders) {
                if (order.getPaymentStatus() != PaymentStatus.PAID) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    orderRepository.save(order);
                }
            }

            Payment payment = paymentRepository.findByGroupOrderCode(groupOrderCode).stream()
                    .findFirst()
                    .orElse(null);
            if (payment != null && payment.getStatus() != PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setTransactionNo(transactionNo);
                paymentRepository.save(payment);
            }

            response.put("status", "SUCCESS");
            response.put("message", "Giao dịch thanh toán qua VNPay thành công!");
        } else {
            response.put("status", "FAILED");
            response.put("message", "Giao dịch bị hủy hoặc không thành công (Mã lỗi: " + responseCode + ")");
        }

        return response;
    }
}

