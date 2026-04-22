package com.BachLe.E_Wallet.common.config;

import com.BachLe.E_Wallet.common.security.CustomUserDetails;
import com.BachLe.E_Wallet.domain.user.entity.User;
import com.BachLe.E_Wallet.domain.user.repository.UserRepository;
import com.BachLe.E_Wallet.domain.user.service.CustomerUserDetailsService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    // Spring sẽ tự động tiêm cái CustomerUserDetailsService có gắn @Service của vào đây
    private final UserDetailsService userDetailsService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() throws GeneralSecurityException, IOException {
        return new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId)) // QUAN TRỌNG: Đảm bảo token này dành riêng cho App của ta
                .build();
    }

    /*
    Tạo một DaoAuthenticationProvider
    để Spring biết cách xác thực User(lấy user từ db + pw)

    (việc xác thực user sẽ do DaoAuthenticationProvider đảm nhận)

    */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration autConfig) throws Exception {
        return autConfig.getAuthenticationManager();
    }
}
