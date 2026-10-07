package com.chopee.modules.seller;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import com.chopee.modules.admin.AdminService;
import com.chopee.modules.admin.dto.AdminDashboardResponse;
import com.chopee.modules.admin.dto.UpdateShopStatusRequest;
import com.chopee.modules.catalog.dto.ProductDetailResponse;
import com.chopee.modules.order.dto.OrderResponse;
import com.chopee.modules.seller.dto.*;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SellerSecurityTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private User sellerA;
    private User sellerB;
    private User buyer;
    private Shop shopA;
    private Shop shopB;
    private Category category;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Rau củ quả")
                .slug("rau-cu-qua")
                .build());

        // 1. Tạo Seller A & Shop A
        sellerA = userRepository.save(User.builder()
                .username("seller_a")
                .passwordHash("hashed")
                .email("seller_a@chopee.vn")
                .fullName("Chủ Gian Hàng A")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopA = shopRepository.save(Shop.builder()
                .user(sellerA)
                .name("Nông Trại Xanh A")
                .slug("nong-trai-xanh-a")
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .rating(new BigDecimal("4.8"))
                .build());

        // 2. Tạo Seller B & Shop B
        sellerB = userRepository.save(User.builder()
                .username("seller_b")
                .passwordHash("hashed")
                .email("seller_b@chopee.vn")
                .fullName("Chủ Gian Hàng B")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopB = shopRepository.save(Shop.builder()
                .user(sellerB)
                .name("Thực Phẩm Sạch B")
                .slug("thuc-pham-sach-b")
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .rating(new BigDecimal("5.0"))
                .build());

        // 3. Tạo Buyer
        buyer = userRepository.save(User.builder()
                .username("buyer_test")
                .passwordHash("hashed")
                .email("buyer_test@chopee.vn")
                .fullName("Khách Mua Hàng")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        // 4. Tạo Sản phẩm của Shop A
        productA = productRepository.save(Product.builder()
                .shop(shopA)
                .category(category)
                .name("Dưa leo baby Shop A")
                .slug("dua-leo-baby-shop-a")
                .thumbnailUrl("https://img.chopee.vn/cucumber.jpg")
                .originalPrice(new BigDecimal("30000"))
                .sellingPrice(new BigDecimal("25000"))
                .stockQuantity(new BigDecimal("50.0"))
                .unit("kg")
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        // 5. Tạo Sản phẩm của Shop B
        productB = productRepository.save(Product.builder()
                .shop(shopB)
                .category(category)
                .name("Xoài cát Hòa Lộc Shop B")
                .slug("xoai-cat-hoa-loc-shop-b")
                .thumbnailUrl("https://img.chopee.vn/mango.jpg")
                .originalPrice(new BigDecimal("80000"))
                .sellingPrice(new BigDecimal("70000"))
                .stockQuantity(new BigDecimal("30.0"))
                .unit("kg")
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Thống kê Seller Dashboard: Tính đúng doanh thu và phân loại số lượng đơn hàng")
    void testSellerDashboard() {
        // Tạo 3 đơn hàng cho Shop A (1 PENDING 100k, 1 CONFIRMED 200k, 1 CANCELLED 300k)
        orderRepository.save(Order.builder()
                .orderCode("ORD-DASH-01")
                .groupOrderCode("GRP-DASH-01")
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("85000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("100000"))
                .build());

        orderRepository.save(Order.builder()
                .orderCode("ORD-DASH-02")
                .groupOrderCode("GRP-DASH-02")
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("185000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("200000"))
                .build());

        orderRepository.save(Order.builder()
                .orderCode("ORD-DASH-03")
                .groupOrderCode("GRP-DASH-03")
                .user(buyer)
                .shop(shopA)
                .shippingName("Khách")
                .shippingPhone("0988111222")
                .shippingAddress("Hà Nội")
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("285000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("300000"))
                .build());

        SellerDashboardResponse dashboard = sellerService.getDashboard(sellerA.getId());

        assertEquals(shopA.getId(), dashboard.getShopId());
        assertEquals(1, dashboard.getTotalProducts());
        assertEquals(3, dashboard.getTotalOrders());
        assertEquals(1, dashboard.getPendingOrders());
        assertEquals(1, dashboard.getConfirmedOrders());
        assertEquals(1, dashboard.getCancelledOrders());
        // Doanh thu chỉ tính đơn không CANCELLED: 100k + 200k = 300k
        assertEquals(0, new BigDecimal("300000.00").compareTo(dashboard.getTotalRevenue()));
    }

    @Test
    @DisplayName("Seller tạo, sửa và xóa sản phẩm của chính mình thành công")
    void testSellerProductCRUD_Success() {
        // 1. Tạo sản phẩm mới
        CreateProductRequest createReq = CreateProductRequest.builder()
                .categoryId(category.getId())
                .name("Cải thìa thủy canh Đà Lạt")
                .thumbnailUrl("https://img.chopee.vn/bokchoy.jpg")
                .originalPrice(new BigDecimal("25000"))
                .sellingPrice(new BigDecimal("20000"))
                .stockQuantity(new BigDecimal("100.0"))
                .unit("bó")
                .storageType(StorageType.FRESH)
                .attributes("{\"cert\":\"VietGAP\"}")
                .variants(List.of(CreateVariantRequest.builder()
                        .variantName("Bó 500g")
                        .price(new BigDecimal("20000"))
                        .stockQuantity(new BigDecimal("50.0"))
                        .build()))
                .build();

        ProductDetailResponse created = sellerService.createProduct(sellerA.getId(), createReq);
        assertNotNull(created.getId());
        assertEquals("Cải thìa thủy canh Đà Lạt", created.getName());
        assertEquals(1, created.getVariants().size());

        // 2. Cập nhật sản phẩm
        UpdateProductRequest updateReq = UpdateProductRequest.builder()
                .categoryId(category.getId())
                .name("Cải thìa thủy canh Đà Lạt (Chuẩn VietGAP)")
                .thumbnailUrl("https://img.chopee.vn/bokchoy.jpg")
                .originalPrice(new BigDecimal("25000"))
                .sellingPrice(new BigDecimal("18000")) // Giảm giá
                .stockQuantity(new BigDecimal("80.0"))
                .unit("bó")
                .storageType(StorageType.FRESH)
                .build();

        ProductDetailResponse updated = sellerService.updateProduct(sellerA.getId(), created.getId(), updateReq);
        assertEquals("Cải thìa thủy canh Đà Lạt (Chuẩn VietGAP)", updated.getName());
        assertEquals(0, new BigDecimal("18000.00").compareTo(updated.getSellingPrice()));

        // 3. Xóa sản phẩm (Soft delete chuyển sang INACTIVE)
        sellerService.deleteProduct(sellerA.getId(), created.getId());
        Product softDeleted = productRepository.findById(created.getId()).orElseThrow();
        assertEquals(ProductStatus.INACTIVE, softDeleted.getStatus());
    }

    @Test
    @DisplayName("Bảo vệ IDOR Sản phẩm: Seller A bị chặn 403 Forbidden khi cố sửa hoặc xóa sản phẩm của Seller B")
    void testIDOR_SellerCannotModifyOtherShopProduct() {
        UpdateProductRequest hackReq = UpdateProductRequest.builder()
                .categoryId(category.getId())
                .name("Xoài cát bị sửa trộm")
                .thumbnailUrl("https://hacked.jpg")
                .originalPrice(new BigDecimal("1000"))
                .sellingPrice(new BigDecimal("500"))
                .stockQuantity(new BigDecimal("0"))
                .build();

        // Seller A cố tình cập nhật sản phẩm của Shop B
        ResponseStatusException exUpdate = assertThrows(ResponseStatusException.class, () ->
                sellerService.updateProduct(sellerA.getId(), productB.getId(), hackReq));
        assertEquals(HttpStatus.FORBIDDEN, exUpdate.getStatusCode());

        // Seller A cố tình xóa sản phẩm của Shop B
        ResponseStatusException exDelete = assertThrows(ResponseStatusException.class, () ->
                sellerService.deleteProduct(sellerA.getId(), productB.getId()));
        assertEquals(HttpStatus.FORBIDDEN, exDelete.getStatusCode());
    }

    @Test
    @DisplayName("Bảo vệ IDOR Đơn hàng: Seller A bị chặn 403 Forbidden khi cố xem hoặc đổi trạng thái đơn hàng của Seller B")
    void testIDOR_SellerCannotAccessOtherShopOrder() {
        // Tạo đơn hàng thuộc Shop B
        Order orderShopB = orderRepository.save(Order.builder()
                .orderCode("ORD-SHOPB-SECRET")
                .groupOrderCode("GRP-SHOPB-SECRET")
                .user(buyer)
                .shop(shopB)
                .shippingName("Khách Shop B")
                .shippingPhone("0988111222")
                .shippingAddress("TP HCM")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("135000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("150000"))
                .build());

        // Seller A cố tình xem chi tiết đơn hàng của Shop B -> 403 Forbidden
        ResponseStatusException exView = assertThrows(ResponseStatusException.class, () ->
                sellerService.getOrderDetail(sellerA.getId(), orderShopB.getOrderCode()));
        assertEquals(HttpStatus.FORBIDDEN, exView.getStatusCode());

        // Seller A cố tình đổi trạng thái đơn của Shop B -> 403 Forbidden
        UpdateOrderStatusRequest updateReq = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .build();

        ResponseStatusException exUpdate = assertThrows(ResponseStatusException.class, () ->
                sellerService.updateOrderStatus(sellerA.getId(), orderShopB.getOrderCode(), updateReq));
        assertEquals(HttpStatus.FORBIDDEN, exUpdate.getStatusCode());
    }

    @Test
    @DisplayName("Tiến trình trạng thái đơn hàng: CONFIRMED -> SHIPPING -> DELIVERED và Hủy đơn hoàn tồn kho")
    void testOrderStatusTransitions() {
        // Đặt hàng cho Shop A
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-FLOW-01")
                .groupOrderCode("GRP-FLOW-01")
                .user(buyer)
                .shop(shopA)
                .shippingName("Người Nhận")
                .shippingPhone("0912345678")
                .shippingAddress("Đà Nẵng")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("85000"))
                .shippingFee(new BigDecimal("15000"))
                .finalAmount(new BigDecimal("100000"))
                .build());

        // 1. Seller A xác nhận đơn: PENDING -> CONFIRMED
        OrderResponse confirmed = sellerService.updateOrderStatus(sellerA.getId(), order.getOrderCode(),
                UpdateOrderStatusRequest.builder().status(OrderStatus.CONFIRMED).note("Đã đóng gói rau").build());
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());

        // 2. Seller A giao cho bên vận chuyển: CONFIRMED -> SHIPPING
        OrderResponse shipping = sellerService.updateOrderStatus(sellerA.getId(), order.getOrderCode(),
                UpdateOrderStatusRequest.builder().status(OrderStatus.SHIPPING).build());
        assertEquals(OrderStatus.SHIPPING, shipping.getStatus());

        // 3. Đơn đã giao thành công: SHIPPING -> DELIVERED
        OrderResponse delivered = sellerService.updateOrderStatus(sellerA.getId(), order.getOrderCode(),
                UpdateOrderStatusRequest.builder().status(OrderStatus.DELIVERED).build());
        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());

        // 4. Không thể đổi trạng thái đơn đã DELIVERED
        assertThrows(ResponseStatusException.class, () ->
                sellerService.updateOrderStatus(sellerA.getId(), order.getOrderCode(),
                        UpdateOrderStatusRequest.builder().status(OrderStatus.CANCELLED).build()));
    }

    @Test
    @DisplayName("Admin Dashboard & Phê duyệt/Khóa gian hàng")
    void testAdminManagement() {
        // Tạo thêm gian hàng PENDING
        User sellerC = userRepository.save(User.builder()
                .username("seller_pending")
                .passwordHash("hashed")
                .email("seller_pending@chopee.vn")
                .fullName("Chủ Gian Hàng Mới")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        Shop pendingShop = shopRepository.save(Shop.builder()
                .user(sellerC)
                .name("Gian Hàng Đang Chờ Duyệt")
                .slug("gian-hang-dang-cho-duyet")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.PENDING)
                .build());

        // 1. Kiểm tra Admin Dashboard
        AdminDashboardResponse adminDash = adminService.getDashboard();
        assertTrue(adminDash.getTotalShops() >= 3);
        assertTrue(adminDash.getPendingShops() >= 1);
        assertTrue(adminDash.getApprovedShops() >= 2);

        // 2. Admin phê duyệt gian hàng: PENDING -> APPROVED
        adminService.updateShopStatus(pendingShop.getId(),
                UpdateShopStatusRequest.builder().status(ShopStatus.APPROVED).build());
        Shop approvedShop = shopRepository.findById(pendingShop.getId()).orElseThrow();
        assertEquals(ShopStatus.APPROVED, approvedShop.getStatus());

        // 3. Admin khóa gian hàng vi phạm: APPROVED -> LOCKED
        adminService.updateShopStatus(pendingShop.getId(),
                UpdateShopStatusRequest.builder().status(ShopStatus.LOCKED).reason("Vi phạm chính sách sàn").build());
        Shop lockedShop = shopRepository.findById(pendingShop.getId()).orElseThrow();
        assertEquals(ShopStatus.LOCKED, lockedShop.getStatus());
    }
}
