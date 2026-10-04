package com.chopee.repository;

import com.chopee.entity.Product;
import com.chopee.entity.enums.ProductStatus;
import com.chopee.entity.enums.StorageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    long countByShopIdAndStatus(Long shopId, ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    Page<Product> findByShopId(Long shopId, Pageable pageable);

    List<Product> findByStorageTypeAndStatus(StorageType storageType, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.origin) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Thao tác trừ tồn kho nguyên tử (Atomic Conditional Deduction):
     * Chỉ trừ tồn kho khi stockQuantity >= quantity yêu cầu, chống triệt để tình trạng bán vượt tồn kho (Overselling).
     * Trả về số dòng cập nhật (1 = thành công, 0 = không đủ hàng).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty, p.soldQuantity = p.soldQuantity + :qty " +
            "WHERE p.id = :id AND p.stockQuantity >= :qty")
    int deductStock(@Param("id") Long id, @Param("qty") BigDecimal qty);

    /**
     * Hoàn lại số lượng tồn kho khi đơn hàng bị hủy
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty, p.soldQuantity = p.soldQuantity - :qty " +
            "WHERE p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("qty") BigDecimal qty);
}
