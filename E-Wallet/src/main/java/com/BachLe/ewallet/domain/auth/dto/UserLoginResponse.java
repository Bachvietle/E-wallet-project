package com.BachLe.ewallet.domain.auth.dto;

import com.BachLe.ewallet.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginResponse {

    private String accessToken;

    private String refreshToken;

    private User user;
}
