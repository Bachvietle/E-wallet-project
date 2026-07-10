package com.BachLe.ewallet.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank
    String email;

    @NotBlank
    String passWord;
}
