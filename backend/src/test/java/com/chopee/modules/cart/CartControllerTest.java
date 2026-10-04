package com.chopee.modules.cart;

import com.chopee.entity.Category;
import com.chopee.entity.Product;
import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.*;
import com.chopee.modules.cart.dto.AddToCartRequest;
import com.chopee.modules.cart.dto.UpdateCartItemRequest;
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
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

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
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private String buyerToken;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Người mua & Sinh Token
        buyer = userRepository.save(User.builder()
                .username("buyer_test")
                .passwordHash(passwordEncoder.encode("Password123"))
                .email("buyer_test@chopee.vn")
                .fullName("Người Mua Test")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        buyerToken = tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(buyer));

        // 2. Tạo Người bán & Shop
        User seller = userRepository.save(User.builder()
                .username("seller_test")
                .passwordHash(passwordEncoder.encode("Password123"))
                .email("seller_test@chopee.vn")
                .fullName("Chủ Cửa Hàng")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        Shop shop = shopRepository.save(Shop.builder()
                .user(seller)
                .name("Siêu Thị Mini")
                .slug("sieu-thi-mini")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Đồ gia dụng")
                .slug("do-gia-dung")
                .build());

        // 3. Tạo Sản phẩm
        testProduct = productRepository.save(Product.builder()
                .shop(shop)
                .category(category)
                .name("Bình giữ nhiệt Lock&Lock 500ml")
                .slug("binh-giu-nhiet-lock-lock-500ml")
                .thumbnailUrl("https://img.chopee.vn/lock.png")
                .originalPrice(new BigDecimal("300000"))
                .sellingPrice(new BigDecimal("250000"))
                .stockQuantity(new BigDecimal("50.0"))
                .unit("chiếc")
                .stepQuantity(new BigDecimal("1.0"))
                .minOrderQuantity(new BigDecimal("1.0"))
                .storageType(StorageType.NORMAL)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Thao tác giỏ hàng khi chưa đăng nhập trả về 401 Unauthorized")
    void testCartWithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Luồng thêm vào giỏ, xem giỏ, sửa số lượng và xóa sản phẩm thành công")
    void testCompleteCartFlow() throws Exception {
        // 1. Thêm sản phẩm vào giỏ
        AddToCartRequest addReq = AddToCartRequest.builder()
                .productId(testProduct.getId())
                .quantity(new BigDecimal("2.0"))
                .build();

        String addResponse = mockMvc.perform(post("/api/v1/buyer/cart/items")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.quantity", is(2.0)))
                .andExpect(jsonPath("$.data.itemSubtotal", is(500000.0)))
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResponse).path("data").path("id").asLong();

        // 2. Xem giỏ hàng
        mockMvc.perform(get("/api/v1/buyer/cart")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalItemCount", is(1)))
                .andExpect(jsonPath("$.data.grandTotal", is(500000.0)))
                .andExpect(jsonPath("$.data.shops[0].shopName", is("Siêu Thị Mini")));

        // 3. Cập nhật số lượng lên 3 chiếc
        UpdateCartItemRequest updateReq = UpdateCartItemRequest.builder()
                .quantity(new BigDecimal("3.0"))
                .build();

        mockMvc.perform(put("/api/v1/buyer/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.quantity", is(3.0)))
                .andExpect(jsonPath("$.data.itemSubtotal", is(750000.0)));

        // 4. Xóa sản phẩm khỏi giỏ
        mockMvc.perform(delete("/api/v1/buyer/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 5. Kiểm tra giỏ hàng rỗng
        mockMvc.perform(get("/api/v1/buyer/cart")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItemCount", is(0)))
                .andExpect(jsonPath("$.data.grandTotal", is(0)));
    }
}
