package com.BachLe.E_Wallet.domain.user.service;

import com.BachLe.E_Wallet.common.security.CustomUserDetails;
import com.BachLe.E_Wallet.domain.user.entity.User;
import com.BachLe.E_Wallet.domain.user.repository.UserRepository;
import com.BachLe.E_Wallet.domain.wallet.entity.Wallet;
import com.BachLe.E_Wallet.domain.wallet.repository.WalletRepository;
import com.BachLe.E_Wallet.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final WalletService walletService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        UUID walletId = walletService.getWalletIdByUserId(user.getId());

        return new CustomUserDetails(
                user.getId(),
                walletId,
                user.getEmail(),
                user.getPasswordHash(),
                user.isVerified(), // Tự động map vào isEnabled
                user.isActive(),   // Tự động map vào isAccountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
