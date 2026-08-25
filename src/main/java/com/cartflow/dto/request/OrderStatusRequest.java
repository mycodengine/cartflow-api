package com.cartflow.dto.request;

import com.cartflow.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Sent by admins to advance the order state machine. */
public record OrderStatusRequest(@NotNull OrderStatus status) {
}
