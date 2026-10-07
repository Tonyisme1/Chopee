package com.chopee.config;

import com.chopee.entity.Product;
import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.entity.enums.StorageType;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        voucherRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userAddressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Khởi tạo dữ liệu mẫu sàn Chopee: Đủ 7 tài khoản, 5 gian hàng đa ngành, 14 danh mục và hơn 30 sản phẩm")
    void testSeedData_Success() {
        // Thực thi hàm gieo mầm dữ liệu
        dataInitializer.seedData();

        // 1. Kiểm tra Người dùng
        assertEquals(7, userRepository.count(), "Phải khởi tạo đủ 7 tài khoản");
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertEquals(Role.ROLE_ADMIN, admin.getRole());
        assertTrue(passwordEncoder.matches("123456", admin.getPasswordHash()));

        User sellerFood = userRepository.findByUsername("seller_food").orElseThrow();
        assertEquals(Role.ROLE_SELLER, sellerFood.getRole());

        User buyer = userRepository.findByUsername("buyer1").orElseThrow();
        assertEquals(Role.ROLE_BUYER, buyer.getRole());

        // 2. Kiểm tra Gian hàng
        assertEquals(5, shopRepository.count(), "Phải có đúng 5 gian hàng đại diện các ngành hàng");
        Shop foodShop = shopRepository.findBySlug("nong-san-sach-da-lat").orElseThrow();
        assertEquals(ShopStatus.APPROVED, foodShop.getStatus());
        assertEquals("Nông Sản Sạch Đà Lạt", foodShop.getName());

        Shop techShop = shopRepository.findBySlug("techzone-official-store").orElseThrow();
        assertEquals(ShopStatus.APPROVED, techShop.getStatus());

        // 3. Kiểm tra Danh mục đa cấp
        assertTrue(categoryRepository.count() >= 14, "Cây danh mục phải có tối thiểu 14 danh mục gốc và con");
        assertTrue(categoryRepository.findBySlug("thuc-pham-tuoi-song").isPresent());
        assertTrue(categoryRepository.findBySlug("rau-cu-qua-tuoi").isPresent());
        assertTrue(categoryRepository.findBySlug("thiet-bi-gia-dung").isPresent());

        // 4. Kiểm tra Sản phẩm thực tế
        List<Product> products = productRepository.findAll();
        assertTrue(products.size() >= 31, "Phải có tối thiểu 31 sản phẩm mẫu");

        // Kiểm tra sản phẩm nông sản tươi sống (bước nhảy 0.5kg)
        Product tomato = productRepository.findBySlug("ca-chua-beef-da-lat").orElseThrow();
        assertEquals("kg", tomato.getUnit());
        assertEquals(0, new BigDecimal("0.5").compareTo(tomato.getStepQuantity()));
        assertEquals(StorageType.FRESH, tomato.getStorageType());
        assertTrue(tomato.getAttributes().contains("VietGAP"));

        // Kiểm tra sản phẩm đồ gia dụng có thông số bảo hành và công suất
        Product airFryer = productRepository.findBySlug("noi-chien-khong-dau-philips-hd9252").orElseThrow();
        assertTrue(airFryer.getAttributes().contains("Rapid Air"));
        assertTrue(airFryer.getAttributes().contains("1400W"));

        // Kiểm tra sản phẩm đồ uống đóng thùng
        Product beer = productRepository.findBySlug("thung-24-lon-bia-heineken-silver").orElseThrow();
        assertEquals("thùng", beer.getUnit());
        assertTrue(beer.getAttributes().contains("Thùng 24 lon"));
    }
}
