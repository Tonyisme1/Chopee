package com.chopee.modules.catalog;

import com.chopee.common.dto.PageResponse;
import com.chopee.entity.Product;
import com.chopee.entity.Shop;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.modules.catalog.dto.*;
import com.chopee.repository.ProductRepository;
import com.chopee.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> searchProducts(ProductSearchCriteria criteria, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;

        Sort sort = resolveSort(criteria != null ? criteria.getSortBy() : null);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        List<Long> categoryIds = null;
        if (criteria != null && criteria.getCategoryId() != null) {
            categoryIds = categoryService.getCategoryAndDescendantIds(criteria.getCategoryId());
        }

        Specification<Product> spec = ProductSpecification.buildSpecification(criteria, categoryIds);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductSummaryResponse> content = productPage.getContent().stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        return PageResponse.from(productPage, content);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + id));
        return mapToDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(slug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với slug: " + slug));
        return mapToDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ShopPublicResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng với ID: " + id));
        return mapToShopResponse(shop);
    }

    @Transactional(readOnly = true)
    public ShopPublicResponse getShopBySlug(String slug) {
        Shop shop = shopRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy gian hàng với slug: " + slug));
        return mapToShopResponse(shop);
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "sellingPrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "sellingPrice");
            case "sales" -> Sort.by(Sort.Direction.DESC, "soldQuantity");
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAvg");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Integer calculateDiscountPercent(BigDecimal originalPrice, BigDecimal sellingPrice) {
        if (originalPrice == null || sellingPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        if (originalPrice.compareTo(sellingPrice) <= 0) {
            return 0;
        }
        BigDecimal discount = originalPrice.subtract(sellingPrice);
        return discount.multiply(new BigDecimal("100"))
                .divide(originalPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private ProductSummaryResponse mapToSummaryResponse(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(product.getThumbnailUrl())
                .originalPrice(product.getOriginalPrice())
                .sellingPrice(product.getSellingPrice())
                .discountPercent(calculateDiscountPercent(product.getOriginalPrice(), product.getSellingPrice()))
                .stockQuantity(product.getStockQuantity())
                .soldQuantity(product.getSoldQuantity())
                .unit(product.getUnit())
                .stepQuantity(product.getStepQuantity())
                .minOrderQuantity(product.getMinOrderQuantity())
                .storageType(product.getStorageType())
                .origin(product.getOrigin())
                .ratingAvg(product.getRatingAvg())
                .reviewCount(product.getReviewCount())
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getName() : null)
                .shopAddress(product.getShop() != null ? product.getShop().getAddress() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }

    private ProductDetailResponse mapToDetailResponse(Product product) {
        List<ProductImageResponse> imageResponses = new ArrayList<>();
        if (product.getImages() != null) {
            imageResponses = product.getImages().stream()
                    .sorted((img1, img2) -> Integer.compare(
                            img1.getDisplayOrder() != null ? img1.getDisplayOrder() : 0,
                            img2.getDisplayOrder() != null ? img2.getDisplayOrder() : 0
                    ))
                    .map(img -> ProductImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .displayOrder(img.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
        }

        List<ProductVariantResponse> variantResponses = new ArrayList<>();
        if (product.getVariants() != null) {
            variantResponses = product.getVariants().stream()
                    .map(v -> ProductVariantResponse.builder()
                            .id(v.getId())
                            .variantName(v.getVariantName())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .build())
                    .collect(Collectors.toList());
        }

        ShopPublicResponse shopResponse = product.getShop() != null ? mapToShopResponse(product.getShop()) : null;

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .originalPrice(product.getOriginalPrice())
                .sellingPrice(product.getSellingPrice())
                .discountPercent(calculateDiscountPercent(product.getOriginalPrice(), product.getSellingPrice()))
                .stockQuantity(product.getStockQuantity())
                .soldQuantity(product.getSoldQuantity())
                .unit(product.getUnit())
                .stepQuantity(product.getStepQuantity())
                .minOrderQuantity(product.getMinOrderQuantity())
                .storageType(product.getStorageType())
                .shelfLife(product.getShelfLife())
                .origin(product.getOrigin())
                .attributes(product.getAttributes())
                .ratingAvg(product.getRatingAvg())
                .reviewCount(product.getReviewCount())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .shop(shopResponse)
                .images(imageResponses)
                .variants(variantResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ShopPublicResponse mapToShopResponse(Shop shop) {
        long totalProducts = productRepository.countByShopIdAndStatus(shop.getId(), ProductStatus.ACTIVE);

        return ShopPublicResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .slug(shop.getSlug())
                .description(shop.getDescription())
                .logoUrl(shop.getLogoUrl())
                .bannerUrl(shop.getBannerUrl())
                .address(shop.getAddress())
                .phone(shop.getPhone())
                .rating(shop.getRating())
                .shopType(shop.getShopType())
                .totalProducts(totalProducts)
                .createdAt(shop.getCreatedAt())
                .build();
    }
}
