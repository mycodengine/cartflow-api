package com.cartflow.dto.request;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Short rating,
        @Size(max = 200) String title,
        String body
) {
}
