package com.BachLe.ewallet.common.security;

import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal( @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        final UUID userId;
        final UUID walletId;

        // 1. Kiểm tra Header Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);


        try {
            Claims claims = jwtService.extraAllClaims(jwt);

            userEmail = claims.getSubject();

            // Lấy đúng key "userId" và ép kiểu an toàn
            userId = UUID.fromString(claims.get("userId", String.class)) ;
            walletId = UUID.fromString(claims.get("walletId", String.class)) ;

            // 2. Nếu có email và chưa được xác thực trong Context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String role = claims.get("role", String.class);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("role:" + role);

                // Dựng Proxy User cực nhanh trên RAM
                CustomUserDetails proxyUserDetails = new CustomUserDetails(
                        userId,
                        walletId,
                        userEmail,
                        "", // Không cần quan tâm password ở bước này vì token đã hợp lệ
                        Collections.singletonList(authority)
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        proxyUserDetails,
                        null,
                        proxyUserDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 3. Set vào SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

        } catch (Exception e){
            logger.error("JWT Authentication failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
