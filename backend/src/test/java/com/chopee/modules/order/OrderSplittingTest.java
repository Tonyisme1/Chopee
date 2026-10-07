package com.chopee.modules.order;

import com.chopee.entity.*;
import com.chopee.entity.enums.*;
import com.chopee.modules.cart.CartService;
import com.chopee.modules.cart.dto.AddToCartRequest;
import com.chopee.modules.order.dto.CheckoutPreviewRequest;
import com.chopee.modules.order.dto.CheckoutPreviewResponse;
import com.chopee.modules.order.dto.CheckoutResultResponse;
import com.chopee.modules.order.dto.CreateOrderRequest;
import com.chopee.modules.order.dto.OrderResponse;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderSplittingTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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

    private User buyer;
    private Shop shopA;
    private Shop shopB;
    private Product productShopA;
    private Product productShopB;

    @BeforeEach
    void setUp() {
        orderStatusHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Người mua
        buyer = userRepository.save(User.builder()
                .username("buyer_split")
                .passwordHash("hashed")
                .email("buyer_split@chopee.vn")
                .fullName("Khách Mua Đa Shop")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        // 2. Tạo Shop A (Nông sản tươi)
        User sellerA = userRepository.save(User.builder()
                .username("seller_a")
                .passwordHash("hashed")
                .email("seller_a@chopee.vn")
                .fullName("Chủ Shop A")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopA = shopRepository.save(Shop.builder()
                .user(sellerA)
                .name("Nông Sản Sạch A")
                .slug("nong-san-sach-a")
                .address("Đà Lạt")
                .phone("0911000111")
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .build());

        // 3. Tạo Shop B (Gia dụng B)
        User sellerB = userRepository.save(User.builder()
                .username("seller_b")
                .passwordHash("hashed")
                .email("seller_b@chopee.vn")
                .fullName("Chủ Shop B")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopB = shopRepository.save(Shop.builder()
                .user(sellerB)
                .name("Gia Dụng Hiện Đại B")
                .slug("gia-dung-hien-dai-b")
                .address("Hồ Chí Minh")
                .phone("0922000222")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        Category cat = categoryRepository.save(Category.builder()
                .name("Tổng hợp")
                .slug("tong-hop")
                .build());

        // 4. Tạo Sản phẩm Shop A (Cà chua: 40.000đ/kg, tồn kho 20.0kg)
        productShopA = productRepository.save(Product.builder()
                .shop(shopA)
                .category(cat)
                .name("Cà chua bi hữu cơ Shop A")
                .slug("ca-chua-bi-huu-co-shop-a")
                .thumbnailUrl("https://img.chopee.vn/tomato.jpg")
                .originalPrice(new BigDecimal("50000"))
                .sellingPrice(new BigDecimal("40000"))
                .stockQuantity(new BigDecimal("20.0"))
                .unit("kg")
                .stepQuantity(new BigDecimal("0.5"))
                .minOrderQuantity(new BigDecimal("0.5"))
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        // 5. Tạo Sản phẩm Shop B (Nồi chiên: 1.000.000đ, tồn kho 5 chiếc)
        productShopB = productRepository.save(Product.builder()
                .shop(shopB)
                .category(cat)
                .name("Nồi chiên không dầu Shop B")
                .slug("noi-chien-khong-dau-shop-b")
                .thumbnailUrl("https://img.chopee.vn/fryer.jpg")
                .originalPrice(new BigDecimal("1200000"))
                .sellingPrice(new BigDecimal("1000000"))
                .stockQuantity(new BigDecimal("5.0"))
                .unit("chiếc")
                .stepQuantity(new BigDecimal("1.0"))
                .minOrderQuantity(new BigDecimal("1.0"))
                .storageType(StorageType.NORMAL)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Xem trước thanh toán (Checkout Preview) tính đúng chi tiết phí ship từng Shop")
    void testCheckoutPreview() {
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopA.getId())
                .quantity(new BigDecimal("2.0")) // 2.0 * 40k = 80k
                .build());

        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopB.getId())
                .quantity(new BigDecimal("1.0")) // 1.0 * 1000k = 1000k
                .build());

        CheckoutPreviewRequest previewReq = CheckoutPreviewRequest.builder()
                .shippingMethod(ShippingMethod.EXPRESS_FRESH) // 25k per shop
                .paymentMethod(PaymentMethod.COD)
                .build();

        CheckoutPreviewResponse preview = orderService.previewCheckout(buyer.getId(), previewReq);

        assertEquals(2, preview.getShops().size());
        // Tiền hàng: 80k + 1000k = 1080k
        assertEquals(0, new BigDecimal("1080000.00").compareTo(preview.getTotalItemsAmount()));
        // Tiền ship: 2 shop * 25k = 50k
        assertEquals(0, new BigDecimal("50000.00").compareTo(preview.getTotalShippingFee()));
        // Tổng thanh toán: 1080k + 50k = 1130k
        assertEquals(0, new BigDecimal("1130000.00").compareTo(preview.getGrandFinalAmount()));
    }

    @Test
    @DisplayName("Đặt hàng đa Shop: Tự động tách thành 2 đơn riêng biệt chung groupOrderCode, trừ tồn kho nguyên tử và xóa giỏ hàng")
    void testMultiVendorOrderSplitting_Success() {
        // Thêm hàng từ Shop A (2.0kg) và Shop B (1 chiếc)
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopA.getId())
                .quantity(new BigDecimal("2.0"))
                .build());

        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopB.getId())
                .quantity(new BigDecimal("1.0"))
                .build());

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingName("Nguyễn Văn Mua")
                .shippingPhone("0988111222")
                .shippingAddress("Số 10 Tràng Thi, Hà Nội")
                .shippingMethod(ShippingMethod.STANDARD) // 15k per shop
                .paymentMethod(PaymentMethod.COD)
                .note("Giao giờ hành chính")
                .build();

        CheckoutResultResponse result = orderService.createOrder(buyer.getId(), request);

        // 1. Kiểm tra số lượng đơn tách
        assertEquals(2, result.getTotalOrdersCreated());
        assertNotNull(result.getGroupOrderCode());
        assertTrue(result.getGroupOrderCode().startsWith("GRP-"));

        List<OrderResponse> orders = result.getOrders();
        assertEquals(2, orders.size());

        // Kiểm tra cả 2 đơn đều chung mã gom nhóm groupOrderCode
        assertEquals(result.getGroupOrderCode(), orders.get(0).getGroupOrderCode());
        assertEquals(result.getGroupOrderCode(), orders.get(1).getGroupOrderCode());

        // Kiểm tra mỗi đơn có mã đơn riêng lẻ duy nhất
        assertNotEquals(orders.get(0).getOrderCode(), orders.get(1).getOrderCode());

        // Kiểm tra đơn Shop A
        OrderResponse orderA = orders.stream()
                .filter(o -> o.getShopId().equals(shopA.getId()))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("80000.00").compareTo(orderA.getTotalAmount()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(orderA.getShippingFee()));
        assertEquals(0, new BigDecimal("95000.00").compareTo(orderA.getFinalAmount()));
        assertEquals(1, orderA.getItems().size());
        assertEquals("kg", orderA.getItems().get(0).getUnit());
        assertEquals(0, new BigDecimal("2.0").compareTo(orderA.getItems().get(0).getQuantity()));

        // Kiểm tra đơn Shop B
        OrderResponse orderB = orders.stream()
                .filter(o -> o.getShopId().equals(shopB.getId()))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("1000000.00").compareTo(orderB.getTotalAmount()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(orderB.getShippingFee()));
        assertEquals(0, new BigDecimal("1015000.00").compareTo(orderB.getFinalAmount()));

        // Tổng tiền toàn bộ = 95k + 1015k = 1110k
        assertEquals(0, new BigDecimal("1110000.00").compareTo(result.getGrandTotal()));

        // 2. Kiểm tra tồn kho đã bị trừ nguyên tử
        Product reloadedA = productRepository.findById(productShopA.getId()).orElseThrow();
        // 20.0 - 2.0 = 18.0
        assertEquals(0, new BigDecimal("18.0").compareTo(reloadedA.getStockQuantity()));
        assertEquals(0, new BigDecimal("2.0").compareTo(reloadedA.getSoldQuantity()));

        Product reloadedB = productRepository.findById(productShopB.getId()).orElseThrow();
        // 5.0 - 1.0 = 4.0
        assertEquals(0, new BigDecimal("4.0").compareTo(reloadedB.getStockQuantity()));
        assertEquals(0, new BigDecimal("1.0").compareTo(reloadedB.getSoldQuantity()));

        // 3. Giỏ hàng đã được xóa sạch
        assertEquals(0, cartItemRepository.findByUserId(buyer.getId()).size());
    }

    @Test
    @DisplayName("Nếu bất kỳ sản phẩm nào không đủ tồn kho, toàn bộ giao dịch rollback (không có đơn nào được tạo và tồn kho giữ nguyên)")
    void testOrderCreation_InsufficientStock_RollsBackAllShops() {
        // Thêm hàng từ Shop A (hợp lệ: 2.0kg < 20.0kg)
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopA.getId())
                .quantity(new BigDecimal("2.0"))
                .build());

        // Giả lập Shop B bị người khác mua hết hàng (tồn kho về 0)
        productShopB.setStockQuantity(BigDecimal.ZERO);
        productRepository.save(productShopB);

        // Thêm hàng từ Shop B (không đủ tồn kho: 1.0 > 0)
        cartItemRepository.save(CartItem.builder()
                .user(buyer)
                .product(productShopB)
                .quantity(new BigDecimal("1.0"))
                .build());

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingName("Người Mua")
                .shippingPhone("0988000111")
                .shippingAddress("Hà Nội")
                .build();

        // Đặt hàng thất bại do Shop B hết hàng
        assertThrows(ResponseStatusException.class, () -> orderService.createOrder(buyer.getId(), request));

        // Kiểm tra tính toàn vẹn ACID:
        // 1. Không có đơn hàng nào được tạo
        assertEquals(0, orderRepository.count());

        // 2. Tồn kho của Shop A vẫn nguyên vẹn 20.0kg (đã được rollback!)
        Product reloadedA = productRepository.findById(productShopA.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("20.0").compareTo(reloadedA.getStockQuantity()));
    }

    @Test
    @DisplayName("Hủy đơn hàng đang PENDING: Hoàn lại số lượng tồn kho nguyên tử chính xác")
    void testOrderCancellation_RestoresStock() {
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopA.getId())
                .quantity(new BigDecimal("3.0"))
                .build());

        CheckoutResultResponse result = orderService.createOrder(buyer.getId(), CreateOrderRequest.builder()
                .shippingName("Khách Hàng")
                .shippingPhone("0912345678")
                .shippingAddress("Đà Nẵng")
                .build());

        // Tồn kho sau khi đặt: 20.0 - 3.0 = 17.0
        Product orderedProduct = productRepository.findById(productShopA.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("17.0").compareTo(orderedProduct.getStockQuantity()));

        String orderCode = result.getOrders().get(0).getOrderCode();

        // Khách hàng hủy đơn
        OrderResponse cancelled = orderService.cancelOrder(buyer.getId(), orderCode, "Đổi ý muốn mua món khác");

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertTrue(cancelled.getNote().contains("Lý do hủy"));

        // Kiểm tra tồn kho được hoàn trả nguyên tử về 20.0kg
        Product restoredProduct = productRepository.findById(productShopA.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("20.0").compareTo(restoredProduct.getStockQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(restoredProduct.getSoldQuantity()));
    }

    @Test
    @DisplayName("Không thể hủy đơn hàng nếu đơn đã qua trạng thái CONFIRMED")
    void testCancelOrder_WhenNotPending_ThrowsBadRequest() {
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(productShopA.getId())
                .quantity(new BigDecimal("1.0"))
                .build());

        CheckoutResultResponse result = orderService.createOrder(buyer.getId(), CreateOrderRequest.builder()
                .shippingName("Khách Hàng")
                .shippingPhone("0912345678")
                .shippingAddress("Đà Nẵng")
                .build());

        Order order = orderRepository.findByOrderCode(result.getOrders().get(0).getOrderCode()).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Hủy đơn đã CONFIRMED -> Báo lỗi 400
        assertThrows(ResponseStatusException.class, () ->
                orderService.cancelOrder(buyer.getId(), order.getOrderCode(), "Hủy đơn muộn"));
    }
}
