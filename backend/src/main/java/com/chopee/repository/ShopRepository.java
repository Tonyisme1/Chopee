package com.chopee.repository;

import com.chopee.entity.Shop;
import com.chopee.entity.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findBySlug(String slug);
    Optional<Shop> findByUserId(Long userId);
    List<Shop> findByStatus(ShopStatus status);
    boolean existsBySlug(String slug);
}

