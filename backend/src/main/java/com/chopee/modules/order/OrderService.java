package com.chopee.modules.order;

import com.chopee.common.dto.PageResponse;
import com.chopee.entity.*;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.entity.enums.PaymentStatus;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.ShippingMethod;
import com.chopee.modules.order.dto.*;
import com.chopee.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(Long userId, CheckoutPreviewRequest request) {
        List<CartItem> cartItems = fetchEligibleCartItems(userId, request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng rỗng hoặc không có sản phẩm hợp lệ để thanh toán");
        }

        Map<Long, List<CartItem>> itemsByShop = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getShop().getId(), LinkedHashMap::new, Collectors.toList()));

        List<ShopCheckoutPreview> shopPreviews = new ArrayList<>();
        BigDecimal totalItemsAmount = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        ShippingMethod shippingMethod = request.getShippingMethod() != null ? request.getShippingMethod() : ShippingMethod.STANDARD;
        BigDecimal shippingFeePerShop = (shippingMethod == ShippingMethod.EXPRESS_FRESH)
                ? new BigDecimal("25000")
                : new BigDecimal("15000");

        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            List<CartItem> shopItems = entry.getValue();
            Shop shop = shopItems.get(0).getProduct().getShop();

            BigDecimal shopItemsTotal = BigDecimal.ZERO;
            List<OrderItemPreview> itemPreviews = new ArrayList<>();

            for (CartItem item : shopItems) {
                Product product = item.getProduct();
                ProductVariant variant = item.getVariant();

                BigDecimal unitPrice = (variant != null && variant.getPrice() != null)
                        ? variant.getPrice()
                        : product.getSellingPrice();

                BigDecimal subtotal = unitPrice.multiply(item.getQuantity());
                shopItemsTotal = shopItemsTotal.add(subtotal);

                itemPreviews.add(OrderItemPreview.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .variantId(variant != null ? variant.getId() : null)
                        .variantName(variant != null ? variant.getVariantName() : null)
                        .unit(product.getUnit())
                        .unitPrice(unitPrice)
                        .quantity(item.getQuantity())
                        .subtotal(subtotal)
                        .build());
            }

            BigDecimal shopFinalTotal = shopItemsTotal.add(shippingFeePerShop);
            totalItemsAmount = totalItemsAmount.add(shopItemsTotal);
            totalShippingFee = totalShippingFee.add(shippingFeePerShop);

            shopPreviews.add(ShopCheckoutPreview.builder()
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .items(itemPreviews)
                    .shopItemsTotal(shopItemsTotal)
                    .shopShippingFee(shippingFeePerShop)
                    .shopDiscount(BigDecimal.ZERO)
                    .shopFinalTotal(shopFinalTotal)
                    .build());
        }

        BigDecimal grandFinalAmount = totalItemsAmount.add(totalShippingFee);

        return CheckoutPreviewResponse.builder()
                .shops(shopPreviews)
                .shippingMethod(shippingMethod)
                .paymentMethod(request.getPaymentMethod())
                .totalItemsAmount(totalItemsAmount)
                .totalShippingFee(totalShippingFee)
                .totalDiscountAmount(BigDecimal.ZERO)
                .grandFinalAmount(grandFinalAmount)
                .build();
    }

    /**
     * Tự động tách đơn hàng theo Shop (Order Splitting) và trừ tồn kho nguyên tử (Atomic Stock Deduction).
     * Toàn bộ thao tác chạy trong một giao dịch ACID, nếu bất kỳ sản phẩm nào không đủ tồn kho,
     * toàn bộ quá trình trừ tồn kho và tạo đơn của tất cả các shop sẽ được ROLLBACK hoàn toàn.
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResultResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"));

        List<CartItem> cartItems = fetchEligibleCartItems(userId, request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng rỗng hoặc không có sản phẩm hợp lệ để đặt hàng");
        }

        // 1. Gom nhóm sản phẩm theo từng Shop
        Map<Long, List<CartItem>> itemsByShop = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getShop().getId(), LinkedHashMap::new, Collectors.toList()));

        // 2. Tạo mã chung gom nhóm đơn hàng (groupOrderCode)
        String groupOrderCode = "GRP-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);

        ShippingMethod shippingMethod = request.getShippingMethod() != null ? request.getShippingMethod() : ShippingMethod.STANDARD;
        BigDecimal shippingFeePerShop = (shippingMethod == ShippingMethod.EXPRESS_FRESH)
                ? new BigDecimal("25000")
                : new BigDecimal("15000");

        List<Order> savedOrders = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        // 3. Xử lý tạo từng đơn hàng riêng biệt cho từng Shop
        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItem> shopItems = entry.getValue();
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin cửa hàng"));

            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String orderCode = "ORD-" + datePrefix + "-" + ThreadLocalRandom.current().nextInt(100000, 999999);

            BigDecimal shopItemsTotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            Order order = Order.builder()
                    .orderCode(orderCode)
                    .groupOrderCode(groupOrderCode)
                    .user(user)
                    .shop(shop)
                    .shippingName(request.getShippingName())
                    .shippingPhone(request.getShippingPhone())
                    .shippingAddress(request.getShippingAddress())
                    .shippingMethod(shippingMethod)
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus(PaymentStatus.UNPAID)
                    .status(OrderStatus.PENDING)
                    .note(request.getNote())
                    .shippingFee(shippingFeePerShop)
                    .discountAmount(BigDecimal.ZERO)
                    .build();

            for (CartItem cartItem : shopItems) {
                Product product = cartItem.getProduct();
                ProductVariant variant = cartItem.getVariant();

                if (product.getStatus() != ProductStatus.ACTIVE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Sản phẩm [" + product.getName() + "] hiện đã ngừng mở bán");
                }

                // Trừ tồn kho nguyên tử (Atomic Conditional Deduction chống overselling)
                int rowsUpdated = productRepository.deductStock(product.getId(), cartItem.getQuantity());
                if (rowsUpdated == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Sản phẩm [" + product.getName() + "] không đủ số lượng tồn kho để đặt hàng. Đơn hàng đã được tự động hủy.");
                }

                BigDecimal unitPrice = (variant != null && variant.getPrice() != null)
                        ? variant.getPrice()
                        : product.getSellingPrice();

                BigDecimal itemSubtotal = unitPrice.multiply(cartItem.getQuantity());
                shopItemsTotal = shopItemsTotal.add(itemSubtotal);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .variant(variant)
                        .productName(product.getName())
                        .unit(product.getUnit())
                        .productPrice(unitPrice)
                        .quantity(cartItem.getQuantity())
                        .subtotal(itemSubtotal)
                        .build();

                orderItems.add(orderItem);
            }

            order.setItems(orderItems);
            order.setTotalAmount(shopItemsTotal);
            order.setFinalAmount(shopItemsTotal.add(shippingFeePerShop));

            Order savedOrder = orderRepository.save(order);
            savedOrders.add(savedOrder);
            grandTotal = grandTotal.add(savedOrder.getFinalAmount());
        }

        // 4. Xóa các món hàng đã đặt khỏi giỏ hàng của người mua
        cartItemRepository.deleteAll(cartItems);

        // 5. Trả về kết quả đặt hàng tổng hợp
        List<OrderResponse> orderResponses = savedOrders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return CheckoutResultResponse.builder()
                .groupOrderCode(groupOrderCode)
                .totalOrdersCreated(savedOrders.size())
                .grandTotal(grandTotal)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .orders(orderResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getBuyerOrders(Long userId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), (size <= 0 || size > 100) ? 20 : size);
        Page<Order> orderPage = (status != null)
                ? orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable)
                : orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<OrderResponse> mappedContent = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.from(orderPage, mappedContent);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(Long userId, String orderCode) {
        Order order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng: " + orderCode));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByGroupCode(Long userId, String groupOrderCode) {
        List<Order> orders = orderRepository.findByGroupOrderCodeAndUserId(groupOrderCode, userId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Long userId, String orderCode, String reason) {
        Order order = orderRepository.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng: " + orderCode));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ có thể hủy đơn hàng khi đơn đang ở trạng thái chờ xác nhận (PENDING)");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            order.setNote((order.getNote() != null ? order.getNote() + " | " : "") + "Lý do hủy: " + reason.trim());
        }

        // Hoàn lại tồn kho đã trừ nguyên tử
        for (OrderItem item : order.getItems()) {
            productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
        }

        Order saved = orderRepository.save(order);
        return mapToOrderResponse(saved);
    }

    private List<CartItem> fetchEligibleCartItems(Long userId, List<Long> cartItemIds) {
        List<CartItem> allItems = cartItemRepository.findByUserId(userId);
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            Set<Long> allowedIds = new HashSet<>(cartItemIds);
            return allItems.stream()
                    .filter(item -> allowedIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }
        return allItems;
    }

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(this::mapToOrderItemResponse).collect(Collectors.toList())
                : Collections.emptyList();

        Long shopId = order.getShop() != null ? order.getShop().getId() : null;
        String shopName = null;
        String shopSlug = null;
        if (shopId != null) {
            Shop shop = shopRepository.findById(shopId).orElse(null);
            if (shop != null) {
                shopName = shop.getName();
                shopSlug = shop.getSlug();
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .groupOrderCode(order.getGroupOrderCode())
                .shopId(shopId)
                .shopName(shopName)
                .shopSlug(shopSlug)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingMethod(order.getShippingMethod())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .note(order.getNote())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        String productSlug = null;
        String thumbnailUrl = null;
        if (productId != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                productSlug = product.getSlug();
                thumbnailUrl = product.getThumbnailUrl();
            }
        }
        ProductVariant variant = item.getVariant();

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(productId)
                .productName(item.getProductName())
                .productSlug(productSlug)
                .thumbnailUrl(thumbnailUrl)
                .variantId(variant != null ? variant.getId() : null)
                .variantName(variant != null ? variant.getVariantName() : null)
                .unit(item.getUnit())
                .productPrice(item.getProductPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
