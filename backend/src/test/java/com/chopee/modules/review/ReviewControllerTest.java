package com.chopee.modules.review;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import com.chopee.modules.review.dto.CreateReviewRequest;
import com.chopee.modules.review.dto.ReplyReviewRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

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
    private CategoryRepository categoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private User seller;
    private Shop shop;
    private Product product;
    private Order order;
    private OrderItem orderItem;
    private String buyerToken;
    private String sellerToken;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        voucherRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        buyer = userRepository.save(User.builder()
                .username("test_review_buyer")
                .passwordHash(passwordEncoder.encode("123456"))
                .email("review_buyer@test.com")
                .fullName("Review Buyer")
                .phone("0900000010")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        seller = userRepository.save(User.builder()
                .username("test_review_seller")
                .passwordHash(passwordEncoder.encode("123456"))
                .email("review_seller@test.com")
                .fullName("Review Seller")
                .phone("0900000011")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shop = shopRepository.save(Shop.builder()
                .user(seller)
                .name("Shop Review Test")
                .slug("shop-review-test")
                .status(ShopStatus.APPROVED)
                .build());

        Category cat = categoryRepository.save(Category.builder()
                .name("Danh mục Test")
                .slug("danh-muc-test")
                .build());

        product = productRepository.save(Product.builder()
                .shop(shop)
                .category(cat)
                .name("Sản Phẩm Đánh Giá Test")
                .slug("san-pham-danh-gia-test")
                .originalPrice(new BigDecimal("120000"))
                .sellingPrice(new BigDecimal("100000"))
                .stockQuantity(new BigDecimal("50"))
                .unit("kg")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .ratingAvg(new BigDecimal("5.0"))
                .reviewCount(0)
                .status(ProductStatus.ACTIVE)
                .build());

        order = orderRepository.save(Order.builder()
                .orderCode("ORD-REVIEW-TEST-001")
                .groupOrderCode("GRP-REVIEW-001")
                .user(buyer)
                .shop(shop)
                .status(OrderStatus.DELIVERED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PAID)
                .shippingMethod(ShippingMethod.STANDARD)
                .totalAmount(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("15000"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("115000"))
                .shippingName("Buyer")
                .shippingPhone("0900000010")
                .shippingAddress("123 Test Street")
                .build());

        orderItem = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .unit("kg")
                .productPrice(product.getSellingPrice())
                .quantity(BigDecimal.ONE)
                .subtotal(product.getSellingPrice())
                .build());

        buyerToken = "Bearer " + tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(buyer));
        sellerToken = "Bearer " + tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(seller));
    }

    @Test
    @DisplayName("Người mua gửi đánh giá 5 sao cho đơn hàng đã giao thành công")
    void testCreateReview_Success() throws Exception {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderItemId(orderItem.getId())
                .rating(5)
                .comment("Hàng rất chất lượng, đóng gói kỹ!")
                .imagesJson("[\"https://example.com/photo.jpg\"]")
                .build();

        mockMvc.perform(post("/api/v1/buyer/reviews")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.rating", is(5)))
                .andExpect(jsonPath("$.data.comment", is("Hàng rất chất lượng, đóng gói kỹ!")));

        // Tra cứu công khai đánh giá của sản phẩm
        mockMvc.perform(get("/api/v1/public/products/" + product.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].userFullName", is("Review Buyer")));
    }

    @Test
    @DisplayName("Người bán phản hồi đánh giá khách hàng (Shop Reply)")
    void testReplyReview_Success() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .product(product)
                .user(buyer)
                .orderItem(orderItem)
                .rating(5)
                .comment("Hàng tươi ngon!")
                .isDeleted(false)
                .build());

        ReplyReviewRequest replyRequest = ReplyReviewRequest.builder()
                .shopReply("Cảm ơn bạn nhiều nhé, lần sau mua shop tặng quà thêm nha!")
                .build();

        mockMvc.perform(put("/api/v1/seller/reviews/" + review.getId() + "/reply")
                        .header("Authorization", sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.shopReply", containsString("Cảm ơn bạn nhiều nhé")));
    }
}
