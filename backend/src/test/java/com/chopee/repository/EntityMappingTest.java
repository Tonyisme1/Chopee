package com.chopee.repository;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EntityMappingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Test
    @DisplayName("TC-01: Tạo Người bán (Seller), Gian hàng (Shop) và liên kết 1-1")
    void testCreateSellerAndShop() {
        User seller = User.builder()
                .username("seller_dalat")
                .email("seller@dalatfarm.vn")
                .passwordHash("$2a$10$hashedpassword")
                .fullName("Nguyễn Văn Nông")
                .role(Role.ROLE_SELLER)
                .build();
        User savedSeller = userRepository.save(seller);

        Shop shop = Shop.builder()
                .user(savedSeller)
                .name("Nông Sản Sạch Đà Lạt")
                .slug("nong-san-sach-da-lat")
                .shopType(ShopType.FOOD_FRESH)
                .address("Đà Lạt, Lâm Đồng")
                .build();
        Shop savedShop = shopRepository.save(shop);

        assertThat(savedShop.getId()).isNotNull();
        assertThat(savedShop.getUser().getUsername()).isEqualTo("seller_dalat");
        assertThat(savedShop.getShopType()).isEqualTo(ShopType.FOOD_FRESH);
    }

    @Test
    @DisplayName("TC-02: Tạo Danh mục cha - con và Sản phẩm thực phẩm tươi sống có thuộc tính JSON")
    void testCreateCategoryAndFreshProduct() {
        Category parentCat = Category.builder()
                .name("Thực Phẩm & Tiêu Dùng")
                .slug("thuc-pham-tieu-dung")
                .build();
        Category savedParent = categoryRepository.save(parentCat);

        Category subCat = Category.builder()
                .name("Rau Củ Quả Sạch")
                .slug("rau-cu-qua-sach")
                .parent(savedParent)
                .build();
        Category savedSub = categoryRepository.save(subCat);

        User seller = userRepository.save(User.builder()
                .username("seller_test")
                .email("seller_test@mail.com")
                .passwordHash("pwd")
                .fullName("Chủ Shop")
                .role(Role.ROLE_SELLER)
                .build());

        Shop shop = shopRepository.save(Shop.builder()
                .user(seller)
                .name("Đà Lạt Farm")
                .slug("da-lat-farm")
                .build());

        String jsonAttributes = "{\"certification\":\"VietGAP\",\"harvest_time\":\"05:00 AM\",\"organic\":true}";

        Product tomato = Product.builder()
                .shop(shop)
                .category(savedSub)
                .name("Cà Chua Beef Hữu Cơ")
                .slug("ca-chua-beef-huu-co")
                .originalPrice(new BigDecimal("35000"))
                .sellingPrice(new BigDecimal("28000"))
                .stockQuantity(new BigDecimal("50.5")) // 50.5 kg
                .unit("kg")
                .minOrderQuantity(new BigDecimal("0.5"))
                .stepQuantity(new BigDecimal("0.5"))
                .storageType(StorageType.FRESH)
                .shelfLife("5 ngày bảo quản mát 4-8°C")
                .origin("Đà Lạt, Lâm Đồng")
                .attributes(jsonAttributes)
                .thumbnailUrl("https://example.com/tomato.jpg")
                .build();

        Product savedProduct = productRepository.save(tomato);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getUnit()).isEqualTo("kg");
        assertThat(savedProduct.getStockQuantity()).isEqualByComparingTo("50.5");
        assertThat(savedProduct.getStorageType()).isEqualTo(StorageType.FRESH);
        assertThat(savedProduct.getAttributes()).contains("VietGAP");
    }

    @Test
    @DisplayName("TC-03: Thêm sản phẩm vào giỏ hàng với số lượng thập phân (0.5kg)")
    void testFractionalCartItem() {
        User buyer = userRepository.save(User.builder()
                .username("buyer1")
                .email("buyer1@gmail.com")
                .passwordHash("pwd")
                .fullName("Khách Mua")
                .build());

        User seller = userRepository.save(User.builder()
                .username("seller2")
                .email("seller2@mail.com")
                .passwordHash("pwd")
                .fullName("Shop 2")
                .role(Role.ROLE_SELLER)
                .build());

        Shop shop = shopRepository.save(Shop.builder()
                .user(seller)
                .name("Shop Nông Sản")
                .slug("shop-nong-san")
                .build());

        Category cat = categoryRepository.save(Category.builder().name("Rau").slug("rau").build());

        Product salad = productRepository.save(Product.builder()
                .shop(shop)
                .category(cat)
                .name("Xà Lách Mỡ Thủy Canh")
                .slug("xa-lach-mo-thuy-canh")
                .originalPrice(new BigDecimal("25000"))
                .sellingPrice(new BigDecimal("20000"))
                .stockQuantity(new BigDecimal("20.0"))
                .unit("kg")
                .thumbnailUrl("https://example.com/salad.jpg")
                .build());

        CartItem cartItem = CartItem.builder()
                .user(buyer)
                .product(salad)
                .quantity(new BigDecimal("0.5")) // Mua 0.5kg
                .build();

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        assertThat(savedCartItem.getId()).isNotNull();
        assertThat(savedCartItem.getQuantity()).isEqualByComparingTo("0.5");
    }

    @Test
    @DisplayName("TC-04: Kiểm tra trừ kho nguyên tử (deductStock)")
    void testAtomicStockDeduction() {
        User seller = userRepository.save(User.builder().username("s3").email("s3@m.com").passwordHash("p").fullName("S3").role(Role.ROLE_SELLER).build());
        Shop shop = shopRepository.save(Shop.builder().user(seller).name("Shop 3").slug("shop-3").build());
        Category cat = categoryRepository.save(Category.builder().name("Cat3").slug("cat3").build());

        Product product = productRepository.save(Product.builder()
                .shop(shop)
                .category(cat)
                .name("Nồi Chiên Không Dầu Philips")
                .slug("noi-chien-philips")
                .originalPrice(new BigDecimal("2000000"))
                .sellingPrice(new BigDecimal("1500000"))
                .stockQuantity(new BigDecimal("5.0"))
                .unit("chiếc")
                .thumbnailUrl("https://example.com/airfryer.jpg")
                .build());

        // Trừ 2 chiếc thành công
        int updated = productRepository.deductStock(product.getId(), new BigDecimal("2.0"));
        assertThat(updated).isEqualTo(1);

        Product refreshed = productRepository.findById(product.getId()).orElseThrow();
        assertThat(refreshed.getStockQuantity()).isEqualByComparingTo("3.0");
        assertThat(refreshed.getSoldQuantity()).isEqualByComparingTo("2.0");

        // Cố trừ 10 chiếc (vượt quá 3 chiếc còn lại) -> Phải thất bại (trả về 0)
        int failedUpdate = productRepository.deductStock(product.getId(), new BigDecimal("10.0"));
        assertThat(failedUpdate).isEqualTo(0);
    }

    @Test
    @DisplayName("TC-05: Kiểm tra Đơn hàng Đa Shop liên kết qua groupOrderCode")
    void testMultiVendorOrderGrouping() {
        User buyer = userRepository.save(User.builder().username("buyer_mv").email("bmv@m.com").passwordHash("p").fullName("Khách").build());
        User s1 = userRepository.save(User.builder().username("s_mv1").email("smv1@m.com").passwordHash("p").fullName("S1").role(Role.ROLE_SELLER).build());
        User s2 = userRepository.save(User.builder().username("s_mv2").email("smv2@m.com").passwordHash("p").fullName("S2").role(Role.ROLE_SELLER).build());

        Shop shop1 = shopRepository.save(Shop.builder().user(s1).name("Shop Rau").slug("shop-rau").build());
        Shop shop2 = shopRepository.save(Shop.builder().user(s2).name("Shop Nước").slug("shop-nuoc").build());

        String commonGroupCode = "GRP-20261003-9999";

        Order order1 = Order.builder()
                .orderCode("ORD-SHOP1-001")
                .groupOrderCode(commonGroupCode)
                .user(buyer)
                .shop(shop1)
                .shippingName("Nguyễn Văn A")
                .shippingPhone("0901234567")
                .shippingAddress("123 Lê Lợi, Q1, HCM")
                .shippingMethod(ShippingMethod.EXPRESS_FRESH)
                .totalAmount(new BigDecimal("50000"))
                .shippingFee(new BigDecimal("30000"))
                .finalAmount(new BigDecimal("80000"))
                .build();

        Order order2 = Order.builder()
                .orderCode("ORD-SHOP2-002")
                .groupOrderCode(commonGroupCode)
                .user(buyer)
                .shop(shop2)
                .shippingName("Nguyễn Văn A")
                .shippingPhone("0901234567")
                .shippingAddress("123 Lê Lợi, Q1, HCM")
                .shippingMethod(ShippingMethod.STANDARD)
                .totalAmount(new BigDecimal("120000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("135000"))
                .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> groupedOrders = orderRepository.findByGroupOrderCode(commonGroupCode);
        assertThat(groupedOrders).hasSize(2);
        assertThat(groupedOrders).extracting("shop.name").containsExactlyInAnyOrder("Shop Rau", "Shop Nước");
    }

    @Test
    @DisplayName("TC-06: Kiểm tra các trường mở rộng mới: hasVariants, weightGrams, discount breakdown, cancelledBy và OrderStatusHistory")
    void testExtendedDatabaseFieldsAndHistory() {
        User seller = userRepository.save(User.builder().username("s_ext").email("sext@m.com").passwordHash("p").fullName("S Ext").role(Role.ROLE_SELLER).build());
        Shop shop = shopRepository.save(Shop.builder().user(seller).name("Shop Ext").slug("shop-ext").build());
        Category cat = categoryRepository.save(Category.builder().name("Gia dụng").slug("gia-dung").build());

        // Kiểm tra Product với hasVariants và weightGrams
        Product product = Product.builder()
                .shop(shop)
                .category(cat)
                .name("Nồi chiên không dầu Philips HD9252")
                .slug("noi-chien-philips-hd9252")
                .thumbnailUrl("https://example.com/airfryer.jpg")
                .originalPrice(new BigDecimal("2500000"))
                .sellingPrice(new BigDecimal("1890000"))
                .stockQuantity(new BigDecimal("20"))
                .unit("chiếc")
                .hasVariants(false)
                .weightGrams(4500)
                .storageType(StorageType.NORMAL)
                .build();
        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct.getHasVariants()).isFalse();
        assertThat(savedProduct.getWeightGrams()).isEqualTo(4500);

        // Kiểm tra Order với chiết khấu phân tách và lý do hủy
        User buyer = userRepository.save(User.builder().username("b_ext").email("bext@m.com").passwordHash("p").fullName("B Ext").build());
        Order order = Order.builder()
                .orderCode("ORD-EXT-001")
                .groupOrderCode("GRP-EXT-001")
                .user(buyer)
                .shop(shop)
                .shippingName("Trần Văn B")
                .shippingPhone("0987654321")
                .shippingAddress("456 Hai Bà Trưng, Q3, HCM")
                .totalAmount(new BigDecimal("1890000"))
                .shippingFee(new BigDecimal("25000"))
                .discountAmount(new BigDecimal("100000"))
                .shopDiscountAmount(new BigDecimal("60000"))
                .platformDiscountAmount(new BigDecimal("40000"))
                .finalAmount(new BigDecimal("1815000"))
                .status(OrderStatus.CANCELLED)
                .cancelledBy(CancelledBy.BUYER)
                .cancellationReason("Đổi ý không mua nữa")
                .build();
        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getShopDiscountAmount()).isEqualByComparingTo("60000");
        assertThat(savedOrder.getPlatformDiscountAmount()).isEqualByComparingTo("40000");
        assertThat(savedOrder.getCancelledBy()).isEqualTo(CancelledBy.BUYER);
        assertThat(savedOrder.getCancellationReason()).isEqualTo("Đổi ý không mua nữa");

        // Kiểm tra OrderStatusHistory
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.CANCELLED)
                .changedBy("Khách hàng")
                .note("Đổi ý không mua nữa")
                .build();
        OrderStatusHistory savedHistory = orderStatusHistoryRepository.save(history);

        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getNewStatus()).isEqualTo(OrderStatus.CANCELLED);
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(savedOrder.getId());
        assertThat(histories).hasSize(1);
    }
}

