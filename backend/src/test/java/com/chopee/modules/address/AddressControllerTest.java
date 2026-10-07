package com.chopee.modules.address;

import com.chopee.entity.User;
import com.chopee.entity.UserAddress;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.address.dto.AddressRequest;
import com.chopee.repository.UserAddressRepository;
import com.chopee.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyerUser;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        userAddressRepository.deleteAll();
        userRepository.deleteAll();

        buyerUser = userRepository.save(User.builder()
                .username("buyer_addr_test")
                .passwordHash(passwordEncoder.encode("123456"))
                .email("buyer_addr@test.com")
                .fullName("Buyer Test")
                .phone("0900000003")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        buyerToken = "Bearer " + tokenProvider.generateTokenFromUserPrincipal(UserPrincipal.create(buyerUser));
    }

    @Test
    @DisplayName("Thêm địa chỉ nhận hàng mới và lấy danh sách thành công")
    void testCreateAndGetAddress() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .receiverName("Nguyễn Văn Nhận")
                .phone("0912345678")
                .province("Hà Nội")
                .district("Quận Hoàn Kiếm")
                .ward("Phường Tràng Tiền")
                .detailAddress("Số 1 Tràng Tiền")
                .isDefault(true)
                .build();

        mockMvc.perform(post("/api/v1/buyer/addresses")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.receiverName", is("Nguyễn Văn Nhận")))
                .andExpect(jsonPath("$.data.isDefault", is(true)));

        mockMvc.perform(get("/api/v1/buyer/addresses")
                        .header("Authorization", buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].province", is("Hà Nội")));
    }

    @Test
    @DisplayName("Đặt địa chỉ làm mặc định và xóa địa chỉ thành công")
    void testSetDefaultAndDeleteAddress() throws Exception {
        UserAddress addr1 = userAddressRepository.save(UserAddress.builder()
                .user(buyerUser)
                .receiverName("Địa chỉ 1")
                .phone("0911111111")
                .province("Hồ Chí Minh")
                .district("Quận 1")
                .ward("Bến Nghé")
                .detailAddress("123 Lê Lợi")
                .isDefault(true)
                .build());

        UserAddress addr2 = userAddressRepository.save(UserAddress.builder()
                .user(buyerUser)
                .receiverName("Địa chỉ 2")
                .phone("0922222222")
                .province("Hồ Chí Minh")
                .district("Quận 3")
                .ward("Võ Thị Sáu")
                .detailAddress("456 Nam Kỳ Khởi Nghĩa")
                .isDefault(false)
                .build());

        // Đổi addr2 thành mặc định
        mockMvc.perform(put("/api/v1/buyer/addresses/" + addr2.getId() + "/default")
                        .header("Authorization", buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault", is(true)));

        // Xóa addr1
        mockMvc.perform(delete("/api/v1/buyer/addresses/" + addr1.getId())
                        .header("Authorization", buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }
}
