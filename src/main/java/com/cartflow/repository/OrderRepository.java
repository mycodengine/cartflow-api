package com.cartflow.repository;

import com.cartflow.domain.entity.Order;
import com.cartflow.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
