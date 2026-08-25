package com.cartflow.event;

import com.cartflow.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Fired every time an order's status transitions. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    private Long orderId;
    private Long userId;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private LocalDateTime changedAt;
}
