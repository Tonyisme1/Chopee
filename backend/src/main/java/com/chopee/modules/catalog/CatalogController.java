package com.chopee.modules.catalog;

import com.chopee.common.dto.ApiResponse;
import com.chopee.common.dto.PageResponse;
import com.chopee.entity.enums.StorageType;
import com.chopee.modules.catalog.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Catalog & Search", description = "Các API công khai: tra cứu danh mục đa ngành hàng, tìm kiếm & xem chi tiết sản phẩm")
public class CatalogController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/categories")
    @Operation(summary = "Lấy cây danh mục sản phẩm", description = "Trả về danh sách danh mục theo cấu trúc cây cha - con hỗ trợ hiển thị menu điều hướng")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Chi tiết danh mục theo ID")
    public ResponseEntity<ApiResponse<CategoryTreeResponse>> getCategoryById(
            @Parameter(description = "ID danh mục") @PathVariable Long id) {
        CategoryTreeResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/products")
    @Operation(summary = "Tìm kiếm & Lọc sản phẩm", description = "Tìm kiếm sản phẩm theo từ khóa, danh mục, khoảng giá, hình thức bảo quản (tươi sống/đông lạnh), đánh giá và sắp xếp")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> searchProducts(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mô tả, xuất xứ, tên shop)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "ID danh mục (sẽ bao gồm cả các danh mục con)")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Giá bán tối thiểu")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Giá bán tối đa")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Điều kiện bảo quản (NORMAL, FRESH, FROZEN_CHILLED)")
            @RequestParam(required = false) StorageType storageType,

            @Parameter(description = "Đơn vị tính (kg, chiếc, bó, hộp, lon...)")
            @RequestParam(required = false) String unit,

            @Parameter(description = "Điểm đánh giá tối thiểu (ví dụ: 4.0)")
            @RequestParam(required = false) BigDecimal minRating,

            @Parameter(description = "Lọc theo ID Shop người bán")
            @RequestParam(required = false) Long shopId,

            @Parameter(description = "Tiêu chí sắp xếp: newest, price_asc, price_desc, sales, rating")
            @RequestParam(defaultValue = "newest") String sortBy,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số phần tử mỗi trang (tối đa 100)")
            @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .storageType(storageType)
                .unit(unit)
                .minRating(minRating)
                .shopId(shopId)
                .sortBy(sortBy)
                .build();

        PageResponse<ProductSummaryResponse> result = productService.searchProducts(criteria, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Xem chi tiết sản phẩm theo ID", description = "Bao gồm thông tin sản phẩm, thuộc tính động JSON, danh sách ảnh, biến thể và gian hàng")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            @Parameter(description = "ID sản phẩm") @PathVariable Long id) {
        ProductDetailResponse detail = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/products/slug/{slug}")
    @Operation(summary = "Xem chi tiết sản phẩm theo slug", description = "Hỗ trợ định tuyến URL thân thiện SEO")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(
            @Parameter(description = "Slug duy nhất của sản phẩm") @PathVariable String slug) {
        ProductDetailResponse detail = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/shops/{id}")
    @Operation(summary = "Xem thông tin gian hàng người bán theo ID", description = "Trả về hồ sơ gian hàng, đánh giá và tổng số sản phẩm đang mở bán")
    public ResponseEntity<ApiResponse<ShopPublicResponse>> getShopById(
            @Parameter(description = "ID gian hàng") @PathVariable Long id) {
        ShopPublicResponse shop = productService.getShopById(id);
        return ResponseEntity.ok(ApiResponse.success(shop));
    }

    @GetMapping("/shops/slug/{slug}")
    @Operation(summary = "Xem thông tin gian hàng theo slug")
    public ResponseEntity<ApiResponse<ShopPublicResponse>> getShopBySlug(
            @Parameter(description = "Slug duy nhất của Shop") @PathVariable String slug) {
        ShopPublicResponse shop = productService.getShopBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(shop));
    }
}
