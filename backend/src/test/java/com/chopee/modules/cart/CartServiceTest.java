package com.chopee.modules.cart;

import com.chopee.entity.*;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.Role;
import com.chopee.entity.enums.ShopStatus;
import com.chopee.entity.enums.ShopType;
import com.chopee.entity.enums.StorageType;
import com.chopee.entity.enums.UserStatus;
import com.chopee.modules.cart.dto.AddToCartRequest;
import com.chopee.modules.cart.dto.CartItemResponse;
import com.chopee.modules.cart.dto.CartResponse;
import com.chopee.modules.cart.dto.UpdateCartItemRequest;
import com.chopee.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired
    private CartService cartService;

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
    private User sellerA;
    private User sellerB;
    private Shop shopA;
    private Shop shopB;
    private Category category;
    private Product freshProductShopA;
    private Product techProductShopB;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        shopRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Người mua
        buyer = userRepository.save(User.builder()
                .username("buyer01")
                .passwordHash("hashed")
                .email("buyer01@chopee.vn")
                .fullName("Khách Mua Hàng")
                .role(Role.ROLE_BUYER)
                .status(UserStatus.ACTIVE)
                .build());

        // 2. Tạo Seller A & Shop A (Thực phẩm tươi sống)
        sellerA = userRepository.save(User.builder()
                .username("sellerA")
                .passwordHash("hashed")
                .email("sellerA@chopee.vn")
                .fullName("Chủ Farm Đà Lạt")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopA = shopRepository.save(Shop.builder()
                .user(sellerA)
                .name("Đà Lạt Farm")
                .slug("da-lat-farm")
                .address("Đà Lạt, Lâm Đồng")
                .phone("0911222333")
                .shopType(ShopType.FOOD_FRESH)
                .status(ShopStatus.APPROVED)
                .build());

        // 3. Tạo Seller B & Shop B (Gia dụng công nghệ)
        sellerB = userRepository.save(User.builder()
                .username("sellerB")
                .passwordHash("hashed")
                .email("sellerB@chopee.vn")
                .fullName("Chủ Mall Gia Dụng")
                .role(Role.ROLE_SELLER)
                .status(UserStatus.ACTIVE)
                .build());

        shopB = shopRepository.save(Shop.builder()
                .user(sellerB)
                .name("Gia Dụng Official Mall")
                .slug("gia-dung-official-mall")
                .address("Hà Nội")
                .phone("0944555666")
                .shopType(ShopType.GENERAL)
                .status(ShopStatus.APPROVED)
                .build());

        // 4. Tạo Danh mục
        category = categoryRepository.save(Category.builder()
                .name("Danh mục chung")
                .slug("danh-muc-chung")
                .build());

        // 5. Tạo Sản phẩm Shop A (Cà chua bi: bán lẻ 0.5kg, giá 40.000đ/kg, tồn kho 20.0kg)
        freshProductShopA = productRepository.save(Product.builder()
                .shop(shopA)
                .category(category)
                .name("Cà chua bi tươi Đà Lạt")
                .slug("ca-chua-bi-tuoi-da-lat")
                .thumbnailUrl("https://img.chopee.vn/tomato.png")
                .originalPrice(new BigDecimal("50000"))
                .sellingPrice(new BigDecimal("40000"))
                .stockQuantity(new BigDecimal("20.0"))
                .unit("kg")
                .stepQuantity(new BigDecimal("0.5"))
                .minOrderQuantity(new BigDecimal("0.5"))
                .storageType(StorageType.FRESH)
                .status(ProductStatus.ACTIVE)
                .build());

        // 6. Tạo Sản phẩm Shop B (Nồi cơm: bán theo chiếc, giá 500.000đ, tồn kho 10 chiếc)
        techProductShopB = productRepository.save(Product.builder()
                .shop(shopB)
                .category(category)
                .name("Nồi cơm điện tử Sharp")
                .slug("noi-com-dien-tu-sharp")
                .thumbnailUrl("https://img.chopee.vn/cooker.png")
                .originalPrice(new BigDecimal("600000"))
                .sellingPrice(new BigDecimal("500000"))
                .stockQuantity(new BigDecimal("10.0"))
                .unit("chiếc")
                .stepQuantity(new BigDecimal("1.0"))
                .minOrderQuantity(new BigDecimal("1.0"))
                .storageType(StorageType.NORMAL)
                .status(ProductStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Thêm thực phẩm tươi sống với số lượng lẻ (1.5kg) thành công")
    void testAddToCart_FractionalQuantitySuccess() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("1.5"))
                .build();

        CartItemResponse response = cartService.addToCart(buyer.getId(), request);

        assertNotNull(response.getId());
        assertEquals(0, new BigDecimal("1.5").compareTo(response.getQuantity()));
        assertEquals("kg", response.getUnit());
        // 1.5kg * 40.000đ = 60.000đ
        assertEquals(0, new BigDecimal("60000.00").compareTo(response.getItemSubtotal()));
        assertTrue(response.isAvailable());
    }

    @Test
    @DisplayName("Thêm số lượng không khớp bước nhảy stepQuantity (1.3kg thay vì bội số 0.5) báo lỗi 400")
    void testAddToCart_InvalidStepQuantity_ThrowsBadRequest() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("1.3"))
                .build();

        assertThrows(ResponseStatusException.class, () -> cartService.addToCart(buyer.getId(), request));
    }

    @Test
    @DisplayName("Thêm số lượng vượt quá tồn kho (25.0kg > 20.0kg) báo lỗi 400")
    void testAddToCart_ExceedsStock_ThrowsBadRequest() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("25.0"))
                .build();

        assertThrows(ResponseStatusException.class, () -> cartService.addToCart(buyer.getId(), request));
    }

    @Test
    @DisplayName("Thêm sản phẩm đã có trong giỏ hàng sẽ cộng dồn số lượng")
    void testAddToCart_ExistingItem_IncrementsQuantity() {
        AddToCartRequest req1 = AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("1.0"))
                .build();
        cartService.addToCart(buyer.getId(), req1);

        AddToCartRequest req2 = AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("1.5"))
                .build();
        CartItemResponse res2 = cartService.addToCart(buyer.getId(), req2);

        assertEquals(0, new BigDecimal("2.5").compareTo(res2.getQuantity()));
        // 2.5 * 40.000 = 100.000
        assertEquals(0, new BigDecimal("100000.00").compareTo(res2.getItemSubtotal()));
    }

    @Test
    @DisplayName("Xem giỏ hàng tự động gom nhóm theo Shop riêng biệt và tính đúng subtotal")
    void testGetCart_MultiVendorGrouping() {
        // Thêm hàng từ Shop A
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("2.0")) // 2.0 * 40.000 = 80.000đ
                .build());

        // Thêm hàng từ Shop B
        cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(techProductShopB.getId())
                .quantity(new BigDecimal("1.0")) // 1.0 * 500.000 = 500.000đ
                .build());

        CartResponse cart = cartService.getCart(buyer.getId());

        assertEquals(2, cart.getTotalItemCount());
        assertEquals(2, cart.getShops().size());

        // Kiểm tra nhóm Shop A
        var groupA = cart.getShops().stream()
                .filter(s -> s.getShopId().equals(shopA.getId()))
                .findFirst().orElseThrow();
        assertEquals("Đà Lạt Farm", groupA.getShopName());
        assertEquals(1, groupA.getItems().size());
        assertEquals(0, new BigDecimal("80000.00").compareTo(groupA.getShopSubtotal()));

        // Kiểm tra nhóm Shop B
        var groupB = cart.getShops().stream()
                .filter(s -> s.getShopId().equals(shopB.getId()))
                .findFirst().orElseThrow();
        assertEquals("Gia Dụng Official Mall", groupB.getShopName());
        assertEquals(1, groupB.getItems().size());
        assertEquals(0, new BigDecimal("500000.00").compareTo(groupB.getShopSubtotal()));

        // Tổng giá trị cả giỏ = 80.000 + 500.000 = 580.000đ
        assertEquals(0, new BigDecimal("580000.00").compareTo(cart.getGrandTotal()));
    }

    @Test
    @DisplayName("Cập nhật số lượng và bảo vệ chống IDOR (không thể sửa/xóa giỏ hàng người khác)")
    void testUpdateAndIdorProtection() {
        CartItemResponse item = cartService.addToCart(buyer.getId(), AddToCartRequest.builder()
                .productId(freshProductShopA.getId())
                .quantity(new BigDecimal("1.0"))
                .build());

        // Người mua hợp lệ cập nhật lên 2.0kg
        CartItemResponse updated = cartService.updateCartItem(buyer.getId(), item.getId(),
                UpdateCartItemRequest.builder().quantity(new BigDecimal("2.0")).build());
        assertEquals(0, new BigDecimal("2.0").compareTo(updated.getQuantity()));

        // Người dùng khác (sellerA) cố tình sửa hoặc xóa mục giỏ hàng này -> Phải ném 404 (IDOR Protection)
        assertThrows(ResponseStatusException.class, () ->
                cartService.updateCartItem(sellerA.getId(), item.getId(),
                        UpdateCartItemRequest.builder().quantity(new BigDecimal("3.0")).build()));

        assertThrows(ResponseStatusException.class, () ->
                cartService.removeCartItem(sellerA.getId(), item.getId()));

        // Người mua hợp lệ xóa sản phẩm
        assertDoesNotThrow(() -> cartService.removeCartItem(buyer.getId(), item.getId()));
        CartResponse emptyCart = cartService.getCart(buyer.getId());
        assertEquals(0, emptyCart.getTotalItemCount());
    }
}
