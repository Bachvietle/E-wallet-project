package com.BachLe.ewallet.common.security;


import com.BachLe.ewallet.domain.auth.entity.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey jwtSecretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ){
        // secretKey lưu dưới dạng BASE64 nên cần decode
        byte [] keyBytes = Decoders.BASE64.decode(secretKey);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public Claims extraAllClaims(String token){

        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token) // Ktra chữ kí (Signature)
                // Ktra expiryDate
                .getPayload();
    }

    public String extractSubject(String token){
        return extraAllClaims(token).getSubject();
    }

    public String generateAccessJwt(CustomUserDetails user){

        String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return Jwts.builder()
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .claim("role", role)
                .claim("userId", user.getId().toString())
                .claim("walletId", user.getWalletId().toString())
                .signWith(jwtSecretKey)
                .compact();
    }

    public String generateRefreshJwt(CustomUserDetails user, HttpServletRequest request){

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getIpAddress(request);

        String refreshToken =  Jwts.builder()
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .claim("ip", ipAddress)
                .signWith(jwtSecretKey)
                .compact();

        return refreshToken;
    }

    // helper để lấy ip (để đoạn code trên gọn hơn)
    public String getIpAddress (HttpServletRequest request){

        String remoteAddress = request.getHeader("X-Forwarded-For");

        return (remoteAddress != null && !remoteAddress.isEmpty()) ? remoteAddress : request.getRemoteAddr();
    }
}
