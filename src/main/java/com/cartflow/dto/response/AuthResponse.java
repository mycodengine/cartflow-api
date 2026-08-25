package com.cartflow.dto.response;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    public static AuthResponse of(String access, String refresh, long expiresIn) {
        return new AuthResponse(access, refresh, "Bearer", expiresIn);
    }
}
