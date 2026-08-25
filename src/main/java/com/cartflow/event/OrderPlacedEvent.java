package com.cartflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fired when a new order is successfully placed at checkout. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal total;
    private String shippingAddress;
    private LocalDateTime placedAt;
}
