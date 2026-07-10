package com.BachLe.ewallet.domain.wallet.repository;

import com.BachLe.ewallet.domain.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")}) // thời gian chờ lấy lock
    @Query("SELECT w FROM Wallet w WHERE w.id = :walletId")
    Optional <Wallet>  getWalletForUpdate (UUID walletId);


    Optional<Wallet> findByUserId(UUID userId);

}
