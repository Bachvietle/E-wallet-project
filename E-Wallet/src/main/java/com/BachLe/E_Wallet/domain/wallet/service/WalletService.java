package com.BachLe.E_Wallet.domain.wallet.service;

import com.BachLe.E_Wallet.domain.wallet.entity.Wallet;
import com.BachLe.E_Wallet.domain.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
