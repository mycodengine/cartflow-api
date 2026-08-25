package com.cartflow.dto.request;

import com.cartflow.domain.enums.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank @Size(max = 50) String code,
        @Size(max = 255) String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @DecimalMin("0") BigDecimal minOrderValue,
        @Min(1) Integer maxUses,
        LocalDateTime expiresAt
) {
}
