package com.BachLe.ewallet.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {

    String email;

    String phoneNumber;

    String name;

    String passWord;
}
