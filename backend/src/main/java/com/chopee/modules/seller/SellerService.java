package com.chopee.modules.seller;

import com.chopee.common.dto.PageResponse;
import com.chopee.entity.*;
import com.chopee.entity.enums.CancelledBy;
import com.chopee.entity.enums.OrderStatus;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.modules.catalog.ProductService;
import com.chopee.modules.catalog.dto.ProductDetailResponse;
import com.chopee.modules.catalog.dto.ProductSummaryResponse;
import com.chopee.modules.order.OrderService;
import com.chopee.modules.order.dto.OrderResponse;
import com.chopee.modules.seller.dto.*;
import com.chopee.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public Shop getSellerShop(Long sellerId) {
        return shopRepository.findByUserId(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng của người bán này"));
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse getDashboard(Long sellerId) {
        Shop shop = getSellerShop(sellerId);
        Long shopId = shop.getId();

        long totalProducts = productRepository.countByShopId(shopId);
        List<Order> shopOrders = orderRepository.findByShopId(shopId);

        long totalOrders = shopOrders.size();
        long pendingOrders = shopOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = shopOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long shippingOrders = shopOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPING).count();
        long deliveredOrders = shopOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = shopOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        BigDecimal totalRevenue = shopOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SellerDashboardResponse.builder()
                .shopId(shop.getId())
                .shopName(shop.getName())
                .shopStatus(shop.getStatus())
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippingOrders(shippingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .ratingAvg(shop.getRating())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(Long sellerId, int page, int size) {
        Shop shop = getSellerShop(sellerId);
        Pageable pageable = PageRequest.of(Math.max(0, page), (size <= 0 || size > 100) ? 20 : size);
        Page<Product> productPage = productRepository.findByShopId(shop.getId(), pageable);

        List<ProductSummaryResponse> content = productPage.getContent().stream()
                .map(productService::mapToSummaryResponse)
                .collect(Collectors.toList());

        return PageResponse.from(productPage, content);
    }

    @Transactional
    public ProductDetailResponse createProduct(Long sellerId, CreateProductRequest request) {
        Shop shop = getSellerShop(sellerId);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục sản phẩm"));

        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? toSlug(request.getSlug().trim())
                : toSlug(request.getName()) + "-" + System.currentTimeMillis();

        if (productRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .originalPrice(request.getOriginalPrice())
                .sellingPrice(request.getSellingPrice())
                .stockQuantity(request.getStockQuantity())
                .unit(request.getUnit() != null ? request.getUnit() : "chiếc")
                .minOrderQuantity(request.getMinOrderQuantity() != null ? request.getMinOrderQuantity() : BigDecimal.ONE)
                .stepQuantity(request.getStepQuantity() != null ? request.getStepQuantity() : BigDecimal.ONE)
                .storageType(request.getStorageType() != null ? request.getStorageType() : com.chopee.entity.enums.StorageType.NORMAL)
                .shelfLife(request.getShelfLife())
                .origin(request.getOrigin())
                .attributes(request.getAttributes())
                .tierVariation(request.getTierVariation())
                .hasVariants(request.getVariants() != null && !request.getVariants().isEmpty())
                .status(ProductStatus.ACTIVE)
                .build();

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                images.add(ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build());
            }
            product.setImages(images);
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (CreateVariantRequest vReq : request.getVariants()) {
                variants.add(ProductVariant.builder()
                        .product(product)
                        .variantName(vReq.getVariantName())
                        .sku(vReq.getSku())
                        .attributes(vReq.getAttributes())
                        .price(vReq.getPrice())
                        .stockQuantity(vReq.getStockQuantity())
                        .build());
            }
            product.setVariants(variants);
        }

        Product saved = productRepository.save(product);
        return productService.mapToDetailResponse(saved);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long sellerId, Long productId, UpdateProductRequest request) {
        Shop shop = getSellerShop(sellerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // IDOR Protection: Đảm bảo sản phẩm thuộc quyền sở hữu của gian hàng người bán
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa sản phẩm của gian hàng khác");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục sản phẩm"));

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setStockQuantity(request.getStockQuantity());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getMinOrderQuantity() != null) product.setMinOrderQuantity(request.getMinOrderQuantity());
        if (request.getStepQuantity() != null) product.setStepQuantity(request.getStepQuantity());
        if (request.getStorageType() != null) product.setStorageType(request.getStorageType());
        if (request.getShelfLife() != null) product.setShelfLife(request.getShelfLife());
        if (request.getOrigin() != null) product.setOrigin(request.getOrigin());
        if (request.getAttributes() != null) product.setAttributes(request.getAttributes());
        if (request.getTierVariation() != null) product.setTierVariation(request.getTierVariation());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        if (request.getImageUrls() != null) {
            product.getImages().clear();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                product.getImages().add(ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .displayOrder(i)
                        .build());
            }
        }

        if (request.getVariants() != null) {
            product.getVariants().clear();
            product.setHasVariants(!request.getVariants().isEmpty());
            for (CreateVariantRequest vReq : request.getVariants()) {
                product.getVariants().add(ProductVariant.builder()
                        .product(product)
                        .variantName(vReq.getVariantName())
                        .sku(vReq.getSku())
                        .attributes(vReq.getAttributes())
                        .price(vReq.getPrice())
                        .stockQuantity(vReq.getStockQuantity())
                        .build());
            }
        }

        Product saved = productRepository.save(product);
        return productService.mapToDetailResponse(saved);
    }

    @Transactional
    public ProductDetailResponse duplicateProduct(Long sellerId, Long productId) {
        Shop shop = getSellerShop(sellerId);
        Product original = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // IDOR Protection: Đảm bảo sản phẩm thuộc shop của seller
        if (!original.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền sao chép sản phẩm của shop khác");
        }

        String newName = "[Bản sao] " + original.getName();
        String baseSlug = toSlug(newName);
        String uniqueSlug = baseSlug + "-" + System.currentTimeMillis();

        Product clone = Product.builder()
                .shop(shop)
                .category(original.getCategory())
                .name(newName)
                .slug(uniqueSlug)
                .description(original.getDescription())
                .thumbnailUrl(original.getThumbnailUrl())
                .originalPrice(original.getOriginalPrice())
                .sellingPrice(original.getSellingPrice())
                .stockQuantity(original.getStockQuantity())
                .unit(original.getUnit())
                .minOrderQuantity(original.getMinOrderQuantity())
                .stepQuantity(original.getStepQuantity())
                .storageType(original.getStorageType())
                .shelfLife(original.getShelfLife())
                .origin(original.getOrigin())
                .attributes(original.getAttributes())
                .tierVariation(original.getTierVariation())
                .hasVariants(original.getHasVariants())
                .status(ProductStatus.ACTIVE)
                .build();

        if (original.getImages() != null && !original.getImages().isEmpty()) {
            List<ProductImage> cloneImages = new ArrayList<>();
            for (ProductImage img : original.getImages()) {
                cloneImages.add(ProductImage.builder()
                        .product(clone)
                        .imageUrl(img.getImageUrl())
                        .displayOrder(img.getDisplayOrder())
                        .build());
            }
            clone.setImages(cloneImages);
        }

        if (original.getVariants() != null && !original.getVariants().isEmpty()) {
            List<ProductVariant> cloneVariants = new ArrayList<>();
            for (ProductVariant v : original.getVariants()) {
                cloneVariants.add(ProductVariant.builder()
                        .product(clone)
                        .variantName(v.getVariantName())
                        .sku(v.getSku() != null ? v.getSku() + "-COPY" : null)
                        .attributes(v.getAttributes())
                        .price(v.getPrice())
                        .stockQuantity(v.getStockQuantity())
                        .build());
            }
            clone.setVariants(cloneVariants);
        }

        Product saved = productRepository.save(clone);
        return productService.mapToDetailResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Shop shop = getSellerShop(sellerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // IDOR Protection: Đảm bảo sản phẩm thuộc gian hàng của người bán
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa sản phẩm của gian hàng khác");
        }

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(Long sellerId, OrderStatus status, int page, int size) {
        Shop shop = getSellerShop(sellerId);
        Pageable pageable = PageRequest.of(Math.max(0, page), (size <= 0 || size > 100) ? 20 : size);
        Page<Order> orderPage = (status != null)
                ? orderRepository.findByShopIdAndStatusOrderByCreatedAtDesc(shop.getId(), status, pageable)
                : orderRepository.findByShopIdOrderByCreatedAtDesc(shop.getId(), pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderService::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.from(orderPage, content);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long sellerId, String orderCode) {
        Shop shop = getSellerShop(sellerId);
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng: " + orderCode));

        // IDOR Protection: Người bán không thể xem đơn của gian hàng khác
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem đơn hàng của gian hàng khác");
        }

        return orderService.mapToOrderResponse(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateOrderStatus(Long sellerId, String orderCode, UpdateOrderStatusRequest request) {
        Shop shop = getSellerShop(sellerId);
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng: " + orderCode));

        // IDOR Protection: Người bán không thể sửa đơn của gian hàng khác
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật đơn hàng của gian hàng khác");
        }

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Kiểm tra luồng chuyển trạng thái hợp lệ
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Không thể thay đổi trạng thái đơn hàng đã kết thúc (" + currentStatus + ")");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledBy(CancelledBy.SELLER);
            String cancelReason = (request.getNote() != null && !request.getNote().trim().isEmpty())
                    ? request.getNote().trim() : "Người bán hủy đơn";
            order.setCancellationReason(cancelReason);

            // Hoàn lại tồn kho khi đơn bị hủy
            for (OrderItem item : order.getItems()) {
                productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            order.setNote((order.getNote() != null ? order.getNote() + " | " : "") + request.getNote().trim());
        }

        Order saved = orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(saved)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .changedBy("Người bán (" + shop.getName() + ")")
                .note(request.getNote())
                .build());

        return orderService.mapToOrderResponse(saved);
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return slug.toLowerCase().replaceAll("[^a-z0-9-]", "");
    }
}
