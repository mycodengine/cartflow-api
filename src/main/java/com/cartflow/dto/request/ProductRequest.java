package com.cartflow.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull @Min(0) Integer stock,
        @NotBlank @Size(max = 100) String sku,
        String imageUrl
) {
}
