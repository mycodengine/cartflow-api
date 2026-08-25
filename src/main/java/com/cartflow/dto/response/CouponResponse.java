package com.cartflow.dto.response;

import com.cartflow.domain.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(Long id, String code, String description, DiscountType discountType,
                              BigDecimal discountValue, BigDecimal minOrderValue, Integer maxUses,
                              int usesCount, boolean active, LocalDateTime expiresAt) {
}
