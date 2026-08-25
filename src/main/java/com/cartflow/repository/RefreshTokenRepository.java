package com.cartflow.repository;

import com.cartflow.domain.entity.RefreshToken;
import com.cartflow.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
}
