package com.chopee.repository;

import com.chopee.entity.Order;
import com.chopee.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByOrderCodeAndUserId(String orderCode, Long userId);
    List<Order> findByGroupOrderCode(String groupOrderCode);
    List<Order> findByGroupOrderCodeAndUserId(String groupOrderCode, Long userId);
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);
    Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    Page<Order> findByShopIdAndStatusOrderByCreatedAtDesc(Long shopId, OrderStatus status, Pageable pageable);
}

