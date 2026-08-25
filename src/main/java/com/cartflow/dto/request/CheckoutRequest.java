package com.cartflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Submitted by the user at checkout to place an order. */
public record CheckoutRequest(
        @NotBlank @Size(max = 500, message = "Shipping address too long") String shippingAddress,
        String couponCode,
        @Size(max = 500) String notes
) {
}
