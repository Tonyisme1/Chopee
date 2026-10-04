package com.chopee.repository;

import com.chopee.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByIdAndUserId(Long id, Long userId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantIsNull(Long userId, Long productId);
    long countByUserId(Long userId);
    void deleteByUserId(Long userId);
}

