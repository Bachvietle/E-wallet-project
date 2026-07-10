package com.BachLe.ewallet.domain.user.service;

import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import com.BachLe.ewallet.domain.user.entity.User;
import com.BachLe.ewallet.domain.user.repository.UserRepository;
import com.BachLe.ewallet.domain.wallet.service.WalletService;
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
