package com.chopee.modules.voucher;

import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.Voucher;
import com.chopee.entity.enums.DiscountType;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.voucher.dto.CreateVoucherRequest;
import com.chopee.repository.*;
import com.chopee.security.JwtTokenProvider;
import com.chopee.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User sellerUser;
    private Shop testShop;
    private String adminToken;
    private String sellerToken;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        voucherRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .username("test_admin")
                .passwordHash(passwordEncoder.encode("123456"))
                .email("admin@test.com")
                .fullName("Admin Test")
                .phone("0900000001")
                .role(Role.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        sellerUser = userRepository.save(User.builder()
                .username("test_seller")
                .passwordHash(passwordEncoder.encode("123456"))
                .email("seller@test.com")
                .fullName("Seller Test")
                .phone("0900000002")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        testShop = shopRepository.save(Shop.builder()
                .user(sellerUser)
                .name("Shop Của Seller")
                .slug("shop-seller-test")
                .status(ShopStatus.APPROVED)
                .build());

        adminToken = "Bearer " + tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(adminUser));
        sellerToken = "Bearer " + tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(sellerUser));
    }

    @Test
    @DisplayName("Admin tạo mã voucher toàn sàn thành công")
    void testCreatePlatformVoucher() throws Exception {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("PLATFORM10")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal("50000"))
                .maxDiscountAmount(new BigDecimal("20000"))
                .usageLimit(100)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        mockMvc.perform(post("/api/v1/admin/vouchers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("PLATFORM10")))
                .andExpect(jsonPath("$.data.shopId", nullValue()));
    }

    @Test
    @DisplayName("Seller tạo mã voucher cho gian hàng thành công")
    void testCreateShopVoucher() throws Exception {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("SHOP20K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("20000"))
                .minOrderAmount(new BigDecimal("100000"))
                .usageLimit(50)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        mockMvc.perform(post("/api/v1/seller/vouchers")
                        .header("Authorization", sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("SHOP20K")))
                .andExpect(jsonPath("$.data.shopId", is(testShop.getId().intValue())));
    }

    @Test
    @DisplayName("Kiểm tra hợp lệ và tính mức giảm voucher (Validate Voucher API)")
    void testValidateVoucher() throws Exception {
        Voucher voucher = voucherRepository.save(Voucher.builder()
                .code("TESTPERCENT10")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal("100000"))
                .maxDiscountAmount(new BigDecimal("15000"))
                .usageLimit(100)
                .usedCount(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .isDeleted(false)
                .build());

        // Đơn 200.000đ -> giảm 10% = 20.000đ nhưng bị chặn tối đa 15.000đ -> còn 185.000đ
        mockMvc.perform(get("/api/v1/public/vouchers/validate")
                        .param("code", "TESTPERCENT10")
                        .param("orderAmount", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isValid", is(true)))
                .andExpect(jsonPath("$.data.discountAmount", is(15000.00)))
                .andExpect(jsonPath("$.data.finalAmount", is(185000.00)));
    }
}
