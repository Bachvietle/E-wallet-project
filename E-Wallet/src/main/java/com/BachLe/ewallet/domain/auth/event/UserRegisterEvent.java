package com.BachLe.ewallet.domain.auth.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRegisterEvent {

    String verifyLink;

    String userMail;
}
