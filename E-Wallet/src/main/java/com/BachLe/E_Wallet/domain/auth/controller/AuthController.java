package com.BachLe.E_Wallet.domain.auth.controller;

import com.BachLe.E_Wallet.common.dto.ApiResponse;
import com.BachLe.E_Wallet.domain.auth.dto.GoogleLoginRequest;
import com.BachLe.E_Wallet.domain.auth.dto.UserLoginRequest;
import com.BachLe.E_Wallet.domain.auth.dto.UserLoginResponse;
import com.BachLe.E_Wallet.domain.auth.service.AuthService;
import com.BachLe.E_Wallet.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(){

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login (@RequestBody @Valid UserLoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        UserLoginResponse userLoginResponse = authService.login(request, httpServletRequest);

        Map<String, Object> data = buildDataLogin(userLoginResponse, httpServletResponse);

        return new ResponseEntity<>(ApiResponse.success("Đăng nhập thành công", data), HttpStatus.OK);
    }

    @PostMapping("/loginGoogle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginWithGoogle (@RequestBody @Valid GoogleLoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        UserLoginResponse userLoginResponse = authService.googleLogin(request.getIdToken(), httpServletRequest);

        Map<String, Object> data = buildDataLogin(userLoginResponse, httpServletResponse);

        return new ResponseEntity<>(ApiResponse.success("Đăng nhập thành công", data), HttpStatus.OK);
    }









                  // ---------------------------- helper -----------------------------//
    private Map<String, Object> buildDataLogin(UserLoginResponse userLoginResponse, HttpServletResponse httpServletResponse ){

        User user = userLoginResponse.getUser();

        // Gắn refreshToken vào Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", userLoginResponse.getRefreshToken())
                .httpOnly(true) // Cookie sẽ không thể bị truy cập bởi JavaScript thông qua document.cookie secure
                .secure(false) // để tạm dùng trong MT dev (chạy local dùng http)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict") // để tạm dùng trong MT dev
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Trả accessToken về body reponse
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", userLoginResponse.getAccessToken());
        data.put("user", Map.of(
                "id", user.getId(), // Nên trả về ID để Frontend dùng
                "email", user.getEmail(),
                "role", user.getRole()
        ));

        return data;
    }

}
