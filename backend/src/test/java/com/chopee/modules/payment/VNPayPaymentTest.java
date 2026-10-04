package com.chopee.modules.payment;

import com.chopee.entity.Order;
import com.chopee.entity.Payment;
import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.*;
import com.chopee.modules.payment.dto.CreatePaymentRequest;
import com.chopee.modules.payment.dto.VNPayIpnResponse;
import com.chopee.modules.payment.dto.VNPayPaymentResponse;
import com.chopee.repository.OrderRepository;
import com.chopee.repository.PaymentRepository;
import com.chopee.repository.ShopRepository;
import com.chopee.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VNPayPaymentTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    private User buyer;
    private Shop shopA;
    private Shop shopB;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        buyer = userRepository.save(User.builder()
                .username("buyer_vnpay")
                .passwordHash("hashed")
                .email("buyer_vnpay@chopee.vn")
                .fullName("Khách Thanh Toán VNPay")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        User sellerA = userRepository.save(User.builder()
                .username("seller_vnpay_a")
                .passwordHash("hashed")
                .email("seller_vnpay_a@chopee.vn")
                .fullName("Chủ Shop A")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopA = shopRepository.save(Shop.builder()
                .user(sellerA)
                .name("Shop A")
                .slug("shop-a")
                .address("Hà Nội")
                .phone("0987111222")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        User sellerB = userRepository.save(User.builder()
                .username("seller_vnpay_b")
                .passwordHash("hashed")
                .email("seller_vnpay_b@chopee.vn")
                .fullName("Chủ Shop B")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopB = shopRepository.save(Shop.builder()
                .user(sellerB)
                .name("Shop B")
                .slug("shop-b")
                .address("Hồ Chí Minh")
                .phone("0987333444")
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .build());
    }

    @Test
    @DisplayName("Kiểm tra tính toán thuật toán băm HMAC-SHA512 và xác thực chữ ký VNPay")
    void testHmacSHA512AndSignatureVerification() {
        String secretKey = "CHOPEEVNPAYSECRETKEY20261004SANDBOX";
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "CHOPEE01");
        params.put("vnp_Amount", "10000000"); // 100,000 VND
        params.put("vnp_TxnRef", "GRP-123456");

        String hashData = VNPayHelper.buildHashData(params);
        String secureHash = VNPayHelper.hmacSHA512(secretKey, hashData);

        assertNotNull(secureHash);
        assertEquals(128, secureHash.length()); // SHA-512 hex output must be 128 characters

        // Thêm hash vào params để verify
        params.put("vnp_SecureHash", secureHash);
        assertTrue(VNPayHelper.verifySignature(params, secretKey));

        // Giả mạo dữ liệu (thay đổi số tiền) -> Chữ ký phải không hợp lệ
        params.put("vnp_Amount", "20000000");
        assertFalse(VNPayHelper.verifySignature(params, secretKey));
    }

    @Test
    @DisplayName("Khởi tạo thanh toán VNPay Sandbox: Tạo đúng URL và bản ghi Payment UNPAID")
    void testCreatePaymentUrl_Success() {
        String groupOrderCode = "GRP-VNPAY-TEST-01";

        // Tạo 2 đơn hàng trong cùng groupOrderCode
        orderRepository.save(Order.builder()
                .orderCode("ORD-SHOPA-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách Hàng")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .shippingMethod(ShippingMethod.STANDARD)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("115000"))
                .build());

        orderRepository.save(Order.builder()
                .orderCode("ORD-SHOPB-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopB)
                .shippingName("Khách Hàng")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .shippingMethod(ShippingMethod.STANDARD)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("215000"))
                .build());

        // Tổng tiền: 115k + 215k = 330k (x100 = 33,000,000)
        VNPayPaymentResponse response = paymentService.createVNPayPayment(
                buyer.getId(),
                CreatePaymentRequest.builder().groupOrderCode(groupOrderCode).bankCode("NCB").build(),
                "127.0.0.1"
        );

        assertNotNull(response.getPaymentUrl());
        assertTrue(response.getPaymentUrl().contains("vnp_Amount=33000000"));
        assertTrue(response.getPaymentUrl().contains("vnp_BankCode=NCB"));
        assertTrue(response.getPaymentUrl().contains("vnp_TxnRef=" + groupOrderCode));
        assertTrue(response.getPaymentUrl().contains("vnp_SecureHash="));
        assertEquals(0, new BigDecimal("330000").compareTo(response.getAmount()));

        // Kiểm tra bản ghi Payment được khởi tạo ở trạng thái UNPAID
        List<Payment> payments = paymentRepository.findByGroupOrderCode(groupOrderCode);
        assertEquals(1, payments.size());
        assertEquals(PaymentStatus.UNPAID, payments.get(0).getStatus());
        assertEquals(0, new BigDecimal("330000.00").compareTo(payments.get(0).getAmount()));
    }

    @Test
    @DisplayName("Xử lý IPN thành công: Tự động cập nhật tất cả đơn hàng con sang PAID")
    void testProcessIpn_Success_UpdatesAllOrdersToPaid() {
        String groupOrderCode = "GRP-VNPAY-TEST-IPN-01";

        Order orderA = orderRepository.save(Order.builder()
                .orderCode("ORD-A-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(new BigDecimal("50000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("65000"))
                .build());

        Order orderB = orderRepository.save(Order.builder()
                .orderCode("ORD-B-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopB)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(new BigDecimal("35000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("50000"))
                .build());

        // Tổng tiền: 65k + 50k = 115k -> 11,500,000
        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", groupOrderCode);
        ipnParams.put("vnp_Amount", "11500000");
        ipnParams.put("vnp_ResponseCode", "00");
        ipnParams.put("vnp_TransactionNo", "VNPay14589234");
        ipnParams.put("vnp_PayDate", "20261004120000");

        String hashData = VNPayHelper.buildHashData(ipnParams);
        String secureHash = VNPayHelper.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        ipnParams.put("vnp_SecureHash", secureHash);

        VNPayIpnResponse ipnResult = paymentService.processIpn(ipnParams);

        assertEquals("00", ipnResult.getRspCode());
        assertEquals("Confirm Success", ipnResult.getMessage());

        // Kiểm tra tất cả đơn hàng đã được cập nhật sang PAID
        Order updatedA = orderRepository.findById(orderA.getId()).orElseThrow();
        Order updatedB = orderRepository.findById(orderB.getId()).orElseThrow();
        assertEquals(PaymentStatus.PAID, updatedA.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, updatedB.getPaymentStatus());

        // Kiểm tra Payment record được cập nhật sang PAID
        List<Payment> payments = paymentRepository.findByGroupOrderCode(groupOrderCode);
        assertEquals(1, payments.size());
        assertEquals(PaymentStatus.PAID, payments.get(0).getStatus());
        assertEquals("VNPay14589234", payments.get(0).getTransactionNo());
    }

    @Test
    @DisplayName("Xử lý IPN với chữ ký sai (Invalid Checksum): Trả về RspCode 97 và không cập nhật đơn hàng")
    void testProcessIpn_InvalidChecksum_Returns97() {
        String groupOrderCode = "GRP-VNPAY-TEST-TAMPER";

        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-TAMPER-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(new BigDecimal("50000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("65000"))
                .build());

        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", groupOrderCode);
        ipnParams.put("vnp_Amount", "6500000");
        ipnParams.put("vnp_ResponseCode", "00");
        ipnParams.put("vnp_SecureHash", "INVALIDSIGNATURE1234567890");

        VNPayIpnResponse ipnResult = paymentService.processIpn(ipnParams);

        assertEquals("97", ipnResult.getRspCode());
        assertEquals("Invalid Checksum", ipnResult.getMessage());

        // Đơn hàng vẫn giữ nguyên UNPAID
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.UNPAID, reloaded.getPaymentStatus());
    }

    @Test
    @DisplayName("Xử lý IPN với đơn hàng không tồn tại: Trả về RspCode 01")
    void testProcessIpn_OrderNotFound_Returns01() {
        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", "GRP-NON-EXISTENT");
        ipnParams.put("vnp_Amount", "5000000");
        ipnParams.put("vnp_ResponseCode", "00");

        String hashData = VNPayHelper.buildHashData(ipnParams);
        String secureHash = VNPayHelper.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        ipnParams.put("vnp_SecureHash", secureHash);

        VNPayIpnResponse ipnResult = paymentService.processIpn(ipnParams);

        assertEquals("01", ipnResult.getRspCode());
        assertEquals("Order not Found", ipnResult.getMessage());
    }

    @Test
    @DisplayName("Xử lý IPN khi đơn hàng đã được thanh toán trước đó: Trả về RspCode 02")
    void testProcessIpn_AlreadyPaid_Returns02() {
        String groupOrderCode = "GRP-VNPAY-ALREADY-PAID";

        orderRepository.save(Order.builder()
                .orderCode("ORD-PAID-01")
                .groupOrderCode(groupOrderCode)
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PAID) // Đã thanh toán
                .totalAmount(new BigDecimal("50000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("65000"))
                .build());

        Map<String, String> ipnParams = new HashMap<>();
        ipnParams.put("vnp_TxnRef", groupOrderCode);
        ipnParams.put("vnp_Amount", "6500000");
        ipnParams.put("vnp_ResponseCode", "00");

        String hashData = VNPayHelper.buildHashData(ipnParams);
        String secureHash = VNPayHelper.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        ipnParams.put("vnp_SecureHash", secureHash);

        VNPayIpnResponse ipnResult = paymentService.processIpn(ipnParams);

        assertEquals("02", ipnResult.getRspCode());
        assertEquals("Order already confirmed", ipnResult.getMessage());
    }
}

