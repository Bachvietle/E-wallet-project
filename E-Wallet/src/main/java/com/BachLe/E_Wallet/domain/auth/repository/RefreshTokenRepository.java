package com.BachLe.E_Wallet.domain.auth.repository;

import com.BachLe.E_Wallet.domain.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime time);

}
