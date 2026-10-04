package com.chopee.modules.catalog;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    private User testSeller;
    private Shop testShop;
    private Category parentCategory;
    private Category childCategory;
    private Product freshProduct;
    private Product frozenProduct;
    private Product techProduct;

    @BeforeEach
    void setUp() {
        productImageRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Seller & Shop
        testSeller = userRepository.save(User.builder()
                .username("seller01")
                .passwordHash("hashedpass")
                .email("seller01@chopee.vn")
                .fullName("Chủ Nông Trại Xanh")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        testShop = shopRepository.save(Shop.builder()
                .user(testSeller)
                .name("Nông Trại Xanh Farm")
                .slug("nong-trai-xanh-farm")
                .description("Chuyên cung cấp rau củ quả hữu cơ VietGAP và hải sản tươi sống")
                .logoUrl("https://img.chopee.vn/farm-logo.png")
                .bannerUrl("https://img.chopee.vn/farm-banner.png")
                .address("123 Đường Nông Nghiệp, Quận Đống Đa, Hà Nội")
                .phone("0988776655")
                .rating(new BigDecimal("4.9"))
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .build());

        // 2. Tạo Phân cấp Danh mục (Cha - Con)
        parentCategory = categoryRepository.save(Category.builder()
                .name("Thực phẩm tươi sống")
                .slug("thuc-pham-tuoi-song")
                .iconUrl("https://img.chopee.vn/cat-fresh.png")
                .displayOrder(1)
                .build());

        childCategory = categoryRepository.save(Category.builder()
                .name("Rau củ hữu cơ Đà Lạt")
                .slug("rau-cu-huu-co-da-lat")
                .parent(parentCategory)
                .iconUrl("https://img.chopee.vn/cat-veg.png")
                .displayOrder(1)
                .build());

        // 3. Tạo các sản phẩm đa ngành hàng & điều kiện bảo quản
        // Thực phẩm tươi sống bán lẻ 0.5kg
        freshProduct = productRepository.save(Product.builder()
                .shop(testShop)
                .category(childCategory)
                .name("Cà chua bi hữu cơ Đà Lạt chuẩn VietGAP")
                .slug("ca-chua-bi-huu-co-da-lat-vietgap")
                .description("Cà chua bi ngọt thanh, trồng chuẩn hữu cơ VietGAP")
                .thumbnailUrl("https://img.chopee.vn/tomato.jpg")
                .originalPrice(new BigDecimal("45000"))
                .sellingPrice(new BigDecimal("36000"))
                .stockQuantity(new BigDecimal("50.0"))
                .soldQuantity(new BigDecimal("120.5"))
                .unit("kg")
                .stepQuantity(new BigDecimal("0.5"))
                .minOrderQuantity(new BigDecimal("0.5"))
                .storageType(StorageType.FRESH)
                .shelfLife("5 ngày bảo quản mát")
                .origin("Đà Lạt, Lâm Đồng")
                .attributes("{\"cert\":\"VietGAP-2026\",\"organic\":true}")
                .ratingAvg(new BigDecimal("4.8"))
                .reviewCount(45)
                .status(ProductStatus.ACTIVE)
                .build());

        // Hải sản đông lạnh
        frozenProduct = productRepository.save(Product.builder()
                .shop(testShop)
                .category(parentCategory)
                .name("Cá thu cắt khúc Phú Quốc cấp đông sâu")
                .slug("ca-thu-cat-khuc-phu-quoc")
                .description("Cá thu biển đánh bắt tự nhiên, cấp đông sâu giữ trọn độ tươi")
                .thumbnailUrl("https://img.chopee.vn/fish.jpg")
                .originalPrice(new BigDecimal("150000"))
                .sellingPrice(new BigDecimal("120000"))
                .stockQuantity(new BigDecimal("30.0"))
                .soldQuantity(new BigDecimal("85.0"))
                .unit("kg")
                .stepQuantity(new BigDecimal("1.0"))
                .minOrderQuantity(new BigDecimal("1.0"))
                .storageType(StorageType.FROZEN_CHILLED)
                .shelfLife("6 tháng trong ngăn đá")
                .origin("Phú Quốc, Kiên Giang")
                .attributes("{\"freeze_temp\":\"-18C\"}")
                .ratingAvg(new BigDecimal("4.9"))
                .reviewCount(30)
                .status(ProductStatus.ACTIVE)
                .build());

        // Thiết bị gia dụng / công nghệ
        techProduct = productRepository.save(Product.builder()
                .shop(testShop)
                .category(parentCategory)
                .name("Nồi chiên không dầu điện tử Philips HD9252")
                .slug("noi-chien-khong-dau-philips-hd9252")
                .description("Nồi chiên dung tích 4.1L, công nghệ Rapid Air giảm 90% dầu mỡ")
                .thumbnailUrl("https://img.chopee.vn/fryer.jpg")
                .originalPrice(new BigDecimal("2100000"))
                .sellingPrice(new BigDecimal("1690000"))
                .stockQuantity(new BigDecimal("15.0"))
                .soldQuantity(new BigDecimal("20.0"))
                .unit("chiếc")
                .stepQuantity(new BigDecimal("1.0"))
                .minOrderQuantity(new BigDecimal("1.0"))
                .storageType(StorageType.NORMAL)
                .shelfLife(null)
                .origin("Chính hãng Philips Hà Lan")
                .attributes("{\"power\":\"1400W\",\"capacity\":\"4.1L\",\"warranty\":\"24 thang\"}")
                .ratingAvg(new BigDecimal("4.7"))
                .reviewCount(18)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/public/categories - Trả về cây danh mục cha - con chính xác")
    void testGetCategoryTree() throws Exception {
        mockMvc.perform(get("/api/v1/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Thực phẩm tươi sống")))
                .andExpect(jsonPath("$.data[0].slug", is("thuc-pham-tuoi-song")))
                .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].name", is("Rau củ hữu cơ Đà Lạt")))
                .andExpect(jsonPath("$.data[0].children[0].slug", is("rau-cu-huu-co-da-lat")));
    }

    @Test
    @DisplayName("GET /api/v1/public/categories/{id} - Lấy chi tiết một danh mục")
    void testGetCategoryById() throws Exception {
        mockMvc.perform(get("/api/v1/public/categories/" + childCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Rau củ hữu cơ Đà Lạt")))
                .andExpect(jsonPath("$.data.parentId", is(parentCategory.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/v1/public/products - Tìm kiếm theo từ khóa 'cà chua'")
    void testSearchProductsByKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/public/products").param("keyword", "cà chua"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content[0].name", containsString("Cà chua bi")))
                .andExpect(jsonPath("$.data.content[0].unit", is("kg")))
                .andExpect(jsonPath("$.data.content[0].stepQuantity", is(0.5)))
                .andExpect(jsonPath("$.data.content[0].discountPercent", is(20)));
    }

    @Test
    @DisplayName("GET /api/v1/public/products - Lọc theo điều kiện bảo quản thực phẩm (FROZEN_CHILLED)")
    void testFilterByStorageType() throws Exception {
        mockMvc.perform(get("/api/v1/public/products").param("storageType", "FROZEN_CHILLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content[0].name", containsString("Cá thu cắt khúc")))
                .andExpect(jsonPath("$.data.content[0].storageType", is("FROZEN_CHILLED")));
    }

    @Test
    @DisplayName("GET /api/v1/public/products - Lọc theo khoảng giá từ 30.000 đến 150.000")
    void testFilterByPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/public/products")
                        .param("minPrice", "30000")
                        .param("maxPrice", "150000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/public/products - Sắp xếp theo giá tăng dần (price_asc)")
    void testSortByPriceAsc() throws Exception {
        mockMvc.perform(get("/api/v1/public/products").param("sortBy", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].sellingPrice", is(36000.0)))
                .andExpect(jsonPath("$.data.content[1].sellingPrice", is(120000.0)))
                .andExpect(jsonPath("$.data.content[2].sellingPrice", is(1690000.0)));
    }

    @Test
    @DisplayName("GET /api/v1/public/products/{id} & /slug/{slug} - Chi tiết sản phẩm với thông tin Shop & Thuộc tính JSON")
    void testGetProductDetail() throws Exception {
        // Test theo ID
        mockMvc.perform(get("/api/v1/public/products/" + freshProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(freshProduct.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is(freshProduct.getName())))
                .andExpect(jsonPath("$.data.shelfLife", is("5 ngày bảo quản mát")))
                .andExpect(jsonPath("$.data.origin", is("Đà Lạt, Lâm Đồng")))
                .andExpect(jsonPath("$.data.attributes", containsString("VietGAP")))
                .andExpect(jsonPath("$.data.shop.name", is("Nông Trại Xanh Farm")));

        // Test theo slug
        mockMvc.perform(get("/api/v1/public/products/slug/" + freshProduct.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(freshProduct.getId().intValue())))
                .andExpect(jsonPath("$.data.slug", is(freshProduct.getSlug())));
    }

    @Test
    @DisplayName("GET /api/v1/public/products/{id} - Sản phẩm INACTIVE trả về 404 Not Found")
    void testGetProductDetailWhenInactive() throws Exception {
        freshProduct.setStatus(ProductStatus.INACTIVE);
        productRepository.save(freshProduct);

        mockMvc.perform(get("/api/v1/public/products/" + freshProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/public/shops/{id} & /slug/{slug} - Xem hồ sơ Shop và tổng số sản phẩm mở bán")
    void testGetShopProfile() throws Exception {
        mockMvc.perform(get("/api/v1/public/shops/" + testShop.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testShop.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("Nông Trại Xanh Farm")))
                .andExpect(jsonPath("$.data.rating", is(4.9)))
                .andExpect(jsonPath("$.data.totalProducts", is(3)));

        mockMvc.perform(get("/api/v1/public/shops/slug/" + testShop.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.slug", is("nong-trai-xanh-farm")));
    }
}
