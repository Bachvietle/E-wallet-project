package com.BachLe.ewallet.domain.auth.entity;

import com.BachLe.ewallet.common.entity.BaseEntity;
import com.BachLe.ewallet.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String token;

    // LAZY fetch cực kỳ quan trọng để tối ưu hiệu năng, tránh Hibernate query thừa bảng User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

}