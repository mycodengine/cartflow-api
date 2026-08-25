package com.cartflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(int status, String message, LocalDateTime timestamp, List<String> errors) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, LocalDateTime.now(), null);
    }
    public static ApiError of(int status, String message, List<String> errors) {
        return new ApiError(status, message, LocalDateTime.now(), errors);
    }
}
