package com.cartflow.service;

import com.cartflow.domain.entity.RefreshToken;
import com.cartflow.domain.entity.User;
import com.cartflow.dto.request.LoginRequest;
import com.cartflow.dto.request.RefreshTokenRequest;
import com.cartflow.dto.request.RegisterRequest;
import com.cartflow.dto.response.AuthResponse;
import com.cartflow.exception.BusinessException;
import com.cartflow.repository.RefreshTokenRepository;
import com.cartflow.repository.UserRepository;
import com.cartflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("User not found"));
        refreshTokenRepository.deleteAllByUser(user);
        log.info("User logged in: {}", user.getEmail());
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token expired — please log in again");
        }
        User user = token.getUser();
        refreshTokenRepository.delete(token);
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefresh = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user).token(rawRefresh)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiryMs * 1_000_000L))
                .build());
        return AuthResponse.of(accessToken, rawRefresh, jwtService.getAccessTokenExpiry());
    }
}
