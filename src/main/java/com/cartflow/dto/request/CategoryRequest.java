package com.cartflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 120) String slug
) {
}
