package com.cartflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String sku,
        String imageUrl,
        boolean active,
        Double averageRating,
        LocalDateTime createdAt
) {
}
