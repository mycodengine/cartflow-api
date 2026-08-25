package com.cartflow.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long userId, List<CartItemResponse> items, BigDecimal total, int totalItems) {
}
