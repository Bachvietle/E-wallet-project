package com.BachLe.ewallet.domain.transaction.repository;

import com.BachLe.ewallet.domain.transaction.entity.TransactionLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, UUID> {

    @Query("SELECT tl FROM TransactionLedger tl JOIN FETCH tl.transaction WHERE tl.walletId = :walletId")
    Page<TransactionLedger> findTransactionLedgerByWalletId(UUID walletId, Pageable pageable);
}
