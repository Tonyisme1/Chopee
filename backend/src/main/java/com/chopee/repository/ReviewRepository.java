package com.chopee.repository;

import com.chopee.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndIsDeletedFalseOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Optional<Review> findByOrderItemId(Long orderItemId);
    boolean existsByOrderItemId(Long orderItemId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isDeleted = false")
    Double calculateAverageRating(@Param("productId") Long productId);

    long countByProductIdAndIsDeletedFalse(Long productId);

    Page<Review> findByProductShopIdAndIsDeletedFalseOrderByCreatedAtDesc(Long shopId, Pageable pageable);
}


