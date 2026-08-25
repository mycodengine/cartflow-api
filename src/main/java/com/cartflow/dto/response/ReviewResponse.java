package com.cartflow.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long productId, Long userId, String userName,
                              short rating, String title, String body, LocalDateTime createdAt) {
}
