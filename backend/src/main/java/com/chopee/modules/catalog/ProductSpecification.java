package com.chopee.modules.catalog;

import com.chopee.entity.Product;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.modules.catalog.dto.ProductSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildSpecification(ProductSearchCriteria criteria, List<Long> categoryIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ tìm các sản phẩm đang ACTIVE cho catalog công khai
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            // Tìm kiếm từ khóa (tên sản phẩm, mô tả, xuất xứ, hoặc tên shop)
            if (criteria != null && criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate originMatch = cb.like(cb.lower(root.get("origin")), pattern);
                Predicate shopMatch = cb.like(cb.lower(root.get("shop").get("name")), pattern);
                predicates.add(cb.or(nameMatch, descMatch, originMatch, shopMatch));
            }

            // Lọc theo danh mục và các danh mục con của nó
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // Lọc theo khoảng giá (minPrice - maxPrice)
            if (criteria != null && criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), criteria.getMinPrice()));
            }
            if (criteria != null && criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), criteria.getMaxPrice()));
            }

            // Lọc theo điều kiện bảo quản thực phẩm (NORMAL, FRESH, FROZEN_CHILLED)
            if (criteria != null && criteria.getStorageType() != null) {
                predicates.add(cb.equal(root.get("storageType"), criteria.getStorageType()));
            }

            // Lọc theo đơn vị tính (kg, chiếc, hộp, v.v.)
            if (criteria != null && criteria.getUnit() != null && !criteria.getUnit().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("unit")), criteria.getUnit().trim().toLowerCase()));
            }

            // Lọc theo điểm đánh giá tối thiểu
            if (criteria != null && criteria.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), criteria.getMinRating()));
            }

            // Lọc theo Shop người bán
            if (criteria != null && criteria.getShopId() != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), criteria.getShopId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
