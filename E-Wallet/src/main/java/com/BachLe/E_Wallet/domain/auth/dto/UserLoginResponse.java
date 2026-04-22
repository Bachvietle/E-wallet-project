package com.BachLe.E_Wallet.domain.auth.dto;

import com.BachLe.E_Wallet.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class UserLoginResponse {

    private String accessToken;

    private String refreshToken;

    private User user;
}
