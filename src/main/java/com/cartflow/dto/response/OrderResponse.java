package com.cartflow.dto.response;

import com.cartflow.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal total,
        String couponCode,
        String shippingAddress,
        String notes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
