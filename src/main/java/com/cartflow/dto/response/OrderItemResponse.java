package com.cartflow.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
}
