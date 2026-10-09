package com.chopee.modules.cart;

import com.chopee.entity.CartItem;
import com.chopee.entity.Product;
import com.chopee.entity.ProductVariant;
import com.chopee.entity.Shop;
import com.chopee.entity.User;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.modules.cart.dto.*;
import com.chopee.repository.CartItemRepository;
import com.chopee.repository.ProductRepository;
import com.chopee.repository.ProductVariantRepository;
import com.chopee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        Map<Long, ShopCartGroupResponse> shopGroups = new LinkedHashMap<>();
        List<CartItemResponse> allItems = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalItemCount = items.size();

        for (CartItem item : items) {
            Product product = item.getProduct();
            Shop shop = product.getShop();
            ProductVariant variant = item.getVariant();

            BigDecimal unitPrice = (variant != null && variant.getPrice() != null)
                    ? variant.getPrice()
                    : product.getSellingPrice();

            BigDecimal stock = (variant != null && variant.getStockQuantity() != null)
                    ? variant.getStockQuantity()
                    : product.getStockQuantity();

            boolean available = (product.getStatus() == ProductStatus.ACTIVE)
                    && (stock != null && stock.compareTo(item.getQuantity()) >= 0);

            BigDecimal itemSubtotal = unitPrice.multiply(item.getQuantity());

            CartItemResponse itemResponse = CartItemResponse.builder()
                    .id(item.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSlug(product.getSlug())
                    .thumbnailUrl(product.getThumbnailUrl())
                    .variantId(variant != null ? variant.getId() : null)
                    .variantName(variant != null ? variant.getVariantName() : null)
                    .unitPrice(unitPrice)
                    .quantity(item.getQuantity())
                    .itemSubtotal(itemSubtotal)
                    .unit(product.getUnit())
                    .stepQuantity(product.getStepQuantity())
                    .minOrderQuantity(product.getMinOrderQuantity())
                    .stockQuantity(stock)
                    .storageType(product.getStorageType())
                    .available(available)
                    .shopId(shop != null ? shop.getId() : null)
                    .shopName(shop != null ? shop.getName() : null)
                    .shopSlug(shop != null ? shop.getSlug() : null)
                    .build();

            ShopCartGroupResponse group = shopGroups.computeIfAbsent(shop.getId(), k -> ShopCartGroupResponse.builder()
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .shopSlug(shop.getSlug())
                    .shopLogoUrl(shop.getLogoUrl())
                    .shopAddress(shop.getAddress())
                    .items(new ArrayList<>())
                    .shopSubtotal(BigDecimal.ZERO)
                    .build());

            group.getItems().add(itemResponse);
            allItems.add(itemResponse);
            if (available) {
                group.setShopSubtotal(group.getShopSubtotal().add(itemSubtotal));
                grandTotal = grandTotal.add(itemSubtotal);
            }
        }

        return CartResponse.builder()
                .shops(new ArrayList<>(shopGroups.values()))
                .items(allItems)
                .totalItemCount(totalItemCount)
                .grandTotal(grandTotal)
                .build();
    }

    @Transactional
    public CartItemResponse addToCart(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm hiện không mở bán");
        }

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Biến thể không thuộc về sản phẩm này");
            }
        }

        validateQuantityConstraints(product, request.getQuantity());

        BigDecimal availableStock = (variant != null && variant.getStockQuantity() != null)
                ? variant.getStockQuantity()
                : product.getStockQuantity();

        Optional<CartItem> existingOpt = (variant != null)
                ? cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, product.getId(), variant.getId())
                : cartItemRepository.findByUserIdAndProductIdAndVariantIsNull(userId, product.getId());

        CartItem cartItem;
        if (existingOpt.isPresent()) {
            cartItem = existingOpt.get();
            BigDecimal newQuantity = cartItem.getQuantity().add(request.getQuantity());

            if (availableStock != null && newQuantity.compareTo(availableStock) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Số lượng trong giỏ hàng (" + newQuantity + ") vượt quá tồn kho hiện có (" + availableStock + ")");
            }

            cartItem.setQuantity(newQuantity);
        } else {
            if (availableStock != null && request.getQuantity().compareTo(availableStock) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Số lượng yêu cầu (" + request.getQuantity() + ") vượt quá tồn kho hiện có (" + availableStock + ")");
            }

            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem saved = cartItemRepository.save(cartItem);
        return mapToItemResponse(saved);
    }

    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng"));

        Product product = cartItem.getProduct();
        ProductVariant variant = cartItem.getVariant();

        validateQuantityConstraints(product, request.getQuantity());

        BigDecimal availableStock = (variant != null && variant.getStockQuantity() != null)
                ? variant.getStockQuantity()
                : product.getStockQuantity();

        if (availableStock != null && request.getQuantity().compareTo(availableStock) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Số lượng yêu cầu (" + request.getQuantity() + ") vượt quá tồn kho hiện có (" + availableStock + ")");
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem saved = cartItemRepository.save(cartItem);
        return mapToItemResponse(saved);
    }

    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng"));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private void validateQuantityConstraints(Product product, BigDecimal quantity) {
        BigDecimal minQty = product.getMinOrderQuantity() != null ? product.getMinOrderQuantity() : BigDecimal.ONE;
        if (quantity.compareTo(minQty) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Số lượng đặt mua tối thiểu là " + minQty + " " + product.getUnit());
        }

        BigDecimal stepQty = product.getStepQuantity() != null ? product.getStepQuantity() : BigDecimal.ONE;
        if (stepQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = quantity.subtract(minQty);
            BigDecimal remainder = diff.remainder(stepQty);
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Số lượng đặt mua phải là bội số của bước nhảy " + stepQty + " " + product.getUnit());
            }
        }
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();

        BigDecimal unitPrice = (variant != null && variant.getPrice() != null)
                ? variant.getPrice()
                : product.getSellingPrice();

        BigDecimal stock = (variant != null && variant.getStockQuantity() != null)
                ? variant.getStockQuantity()
                : product.getStockQuantity();

        boolean available = (product.getStatus() == ProductStatus.ACTIVE)
                && (stock != null && stock.compareTo(item.getQuantity()) >= 0);

        BigDecimal itemSubtotal = unitPrice.multiply(item.getQuantity());

        Shop shop = product.getShop();

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .thumbnailUrl(product.getThumbnailUrl())
                .variantId(variant != null ? variant.getId() : null)
                .variantName(variant != null ? variant.getVariantName() : null)
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .itemSubtotal(itemSubtotal)
                .unit(product.getUnit())
                .stepQuantity(product.getStepQuantity())
                .minOrderQuantity(product.getMinOrderQuantity())
                .stockQuantity(stock)
                .storageType(product.getStorageType())
                .available(available)
                .shopId(shop != null ? shop.getId() : null)
                .shopName(shop != null ? shop.getName() : null)
                .shopSlug(shop != null ? shop.getSlug() : null)
                .build();
    }
}
