package com.cartflow.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(Long id, String name, String description, String slug, LocalDateTime createdAt) {
}
