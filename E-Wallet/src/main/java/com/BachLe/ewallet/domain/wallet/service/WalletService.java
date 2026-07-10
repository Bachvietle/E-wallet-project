package com.BachLe.ewallet.domain.wallet.service;

import com.BachLe.ewallet.domain.wallet.entity.Wallet;
import com.BachLe.ewallet.domain.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public UUID createWallet(UUID userId){
        Wallet newWallet = Wallet.builder()
                .userId(userId).build();

         walletRepository.save(newWallet);

         return newWallet.getId();
    }

    public UUID getWalletIdByUserId(UUID userId){

        return walletRepository.findByUserId(userId)
                .map(wallet -> wallet.getId())
                .orElseThrow();

    }

    @Cacheable(cacheNames = "walletBalance", key = "#walletId")
    public BigDecimal getBalance(UUID walletId){

        Wallet wallet = walletRepository.findById(walletId).orElseThrow();

        return wallet.getBalance();
    }


}
