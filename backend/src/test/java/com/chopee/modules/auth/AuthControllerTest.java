package com.chopee.modules.auth;

import com.chopee.entity.User;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopType;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.auth.dto.LoginRequest;
import com.chopee.modules.auth.dto.RegisterRequest;
import com.chopee.modules.auth.dto.RegisterSellerRequest;
import com.chopee.repository.ShopRepository;
import com.chopee.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        shopRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Đăng ký tài khoản hợp lệ trả về HTTP 201 và Token JWT")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("buyer_test")
                .email("buyer@test.com")
                .password("123456")
                .fullName("Nguyen Van A")
                .phone("0901234567")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("buyer_test"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_BUYER"));
    }

    @Test
    @DisplayName("Đăng ký trùng username hoặc email trả về HTTP 400")
    void testRegisterDuplicate() throws Exception {
        User existingUser = User.builder()
                .username("existing_user")
                .email("existing@test.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .fullName("User Cu")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(existingUser);

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("existing_user")
                .email("another@test.com")
                .password("123456")
                .fullName("User Moi")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Đăng nhập thành công với username và password đúng")
    void testLoginSuccess() throws Exception {
        User user = User.builder()
                .username("test_login")
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Le Thi B")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test_login")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@test.com"));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu trả về HTTP 401")
    void testLoginInvalidPassword() throws Exception {
        User user = User.builder()
                .username("test_login_fail")
                .email("fail@test.com")
                .passwordHash(passwordEncoder.encode("correct_pass"))
                .fullName("Tran Van C")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("test_login_fail")
                .password("wrong_password")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Truy cập /me có Token JWT trả về 200 OK và không có Token trả về 401")
    void testGetProfileMe() throws Exception {
        // 1. Không gửi token -> 401
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());

        // 2. Đăng ký để lấy token
        RegisterRequest register = RegisterRequest.builder()
                .username("token_user")
                .email("token@test.com")
                .password("123456")
                .fullName("Token User")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = jsonNode.path("data").path("accessToken").asText();

        // 3. Gửi token Bearer -> 200 OK
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("token_user"))
                .andExpect(jsonPath("$.data.email").value("token@test.com"));
    }

    @Test
    @DisplayName("Đăng ký Seller nâng cấp User từ ROLE_BUYER thành ROLE_SELLER và tạo Shop")
    void testRegisterSeller() throws Exception {
        // Đăng ký buyer trước
        RegisterRequest register = RegisterRequest.builder()
                .username("future_seller")
                .email("seller@test.com")
                .password("123456")
                .fullName("Chu Shop Dalat")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String buyerToken = jsonNode.path("data").path("accessToken").asText();

        // Đăng ký mở shop
        RegisterSellerRequest sellerRequest = RegisterSellerRequest.builder()
                .shopName("Nông Sản Sạch Đà Lạt")
                .shopDescription("Chuyên rau củ quả hữu cơ tiêu chuẩn VietGAP")
                .shopAddress("123 Phù Đổng Thiên Vương, TP. Đà Lạt")
                .shopPhone("02633888999")
                .shopType(ShopType.FOOD_FRESH)
                .build();

        mockMvc.perform(post("/api/v1/auth/register-seller")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_SELLER"))
                .andExpect(jsonPath("$.data.user.shopId").isNumber())
                .andExpect(jsonPath("$.data.user.shopName").value("Nông Sản Sạch Đà Lạt"));
    }
}
