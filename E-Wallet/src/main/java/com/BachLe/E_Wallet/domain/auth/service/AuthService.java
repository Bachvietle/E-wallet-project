package com.BachLe.E_Wallet.domain.auth.service;

import com.BachLe.E_Wallet.common.entity.CustomUserDetails;
import com.BachLe.E_Wallet.common.security.JwtService;
import com.BachLe.E_Wallet.domain.auth.dto.UserLoginRequest;
import com.BachLe.E_Wallet.domain.auth.dto.UserLoginResponse;
import com.BachLe.E_Wallet.domain.auth.dto.UserRegisterRequest;
import com.BachLe.E_Wallet.domain.auth.entity.RefreshToken;
import com.BachLe.E_Wallet.domain.auth.entity.VerificationToken;
import com.BachLe.E_Wallet.domain.auth.repository.RefreshTokenRepository;
import com.BachLe.E_Wallet.domain.auth.repository.VerificationTokenRepository;
import com.BachLe.E_Wallet.domain.notification.EmailService;
import com.BachLe.E_Wallet.domain.user.entity.User;
import com.BachLe.E_Wallet.domain.user.entity.Role;
import com.BachLe.E_Wallet.domain.auth.entity.TokenType;
import com.BachLe.E_Wallet.domain.user.repository.UserRepository;
import com.BachLe.E_Wallet.domain.wallet.service.WalletService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final WalletService walletService;


    public void register(UserRegisterRequest request) throws MessagingException {

        // 1. Ktra User tồn tại
        Optional<User> existUser = userRepository.findByEmail(request.getEmail());

        if(existUser.isPresent() && existUser.get().isVerified()){
            throw new RuntimeException("");
        }

        // 2. Ghi đè hặc tạo mới User
        User newUser = existUser.orElseGet(() ->
                User.builder()
                        .email(request.getEmail())
                        .build()
                );


        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setUserName(request.getName());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassWord()));
        newUser.setAuthProvider(User.AuthProvider.LOCAL);
        newUser.setRole(Role.USER);

        userRepository.save(newUser);


        // 3. Tạo token verifi Email -> gửi về email verifyLink

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(newUser)
                .tokenType(TokenType.VERIFY_EMAIL)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        verificationTokenRepository.save(verificationToken);

        String verifyLink = "http://localhost:8080/user/auth/verify_register_token?token=" + token; // BE handle endpoint nay (ko phai viet FE)

        emailService.sendVerifyRegisterMail(verifyLink, request.getEmail());

    }

    public boolean verifyRegisterToken(String token) throws IOException {

            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

            if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {

                verificationTokenRepository.delete(verificationToken);

                return false;
            }

            // Cập nhật is_vèified
            User user = verificationToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            // Tài khoản đc xác minh đki thành công thì tạo ví luôn
            walletService.createWallet(user.getId());

            verificationTokenRepository.delete(verificationToken);

            return true;
    }


    public UserLoginResponse login(UserLoginRequest userLoginRequest, HttpServletRequest request){

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getPassWord()
        );

        try {
            Authentication authentication = authenticationManager.authenticate(authRequest);

            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();


            // 2. Sau khi xác thực thành công, tạo token và cho user login
            String accessToken = jwtService.generateAccessJwt(user);
            String refreshToken = jwtService.generateRefreshJwt(user, request);

            /*
            refreshToken sẽ được đưa vào cooke rồi gắn vào header của reponse
            accessToken đuợc đưa lên controller rồi trả về trong Body reponse
            */

            // 3. Lưu refreshToken vào db

            User userProxy = userRepository.getReferenceById(user.getId());

            RefreshToken rt = RefreshToken.builder()
                    .token(refreshToken)
                    .user(userProxy)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .ipAddress(jwtService.getIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            refreshTokenRepository.save(rt);

            return UserLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userProxy)
                    .build();


        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }

    }


    public UserLoginResponse googleLogin(String idTokenString, HttpServletRequest request){
        try {
            // 1. Cấu hình Verifier để xác minh chữ ký của Google

            // việc này đã đc cấu hình Bean trong AppConfig

            // 2. Kiểm tra token
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Google ID Token không hợp lệ hoặc đã hết hạn!");
            }

            // 3. Lấy thông tin an toàn từ Payload của Google
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 4. Xử lý User trong Database
            Optional<User> existUser = userRepository.findByEmail(email);

            User user;

            UUID walletId;

            if (existUser.isPresent()) { // Nếu user đã tồn tại và ko bị ban, bypass ngay
                user = existUser.get();
                // (Optional) Nếu user đang bị ban thì chặn lại ngay
                if (!user.isActive()) {
                    throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
                }

                walletId = walletService.getWalletIdByUserId(user.getId());
            } else {
                // Đăng ký user mới 100% bằng Google
                user = User.builder()
                        .email(email)
                        .userName(name)
                        .authProvider(User.AuthProvider.GOOGLE)
                        .role(Role.USER)
                        .isVerified(true) // Trust Google, không cần gửi email xác thực lại
                        .isActive(true)
                        .build();

                user = userRepository.save(user);

                walletId = walletService.createWallet(user.getId());
            }



            // 5. Đóng gói Proxy User để sinh JWT cực nhanh trên RAM (tương tự như trên)
            CustomUserDetails customUserDetails = new CustomUserDetails(
                    user.getId(),
                    walletId,
                    user.getEmail(),
                    "",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );


            String accessToken = jwtService.generateAccessJwt(customUserDetails);
            String refreshToken = jwtService.generateRefreshJwt(customUserDetails, request);

            User userProxy = userRepository.getReferenceById(user.getId());

            RefreshToken rt = RefreshToken.builder()
                    .token(refreshToken)
                    .user(userProxy)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .ipAddress(jwtService.getIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();
            refreshTokenRepository.save(rt);

            return UserLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userProxy)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }


    /*
     Hàm này để lấy giá trị của name từ cookie.

     (Ở đây sẽ dùng để lấy gtri của refreshToken)
     */
    public String getValueCookie(String name, HttpServletRequest request){

        if(request.getCookies() == null){
            return null;
        }

        return Arrays.stream(request.getCookies()) // Gtri trả về là 1 Array
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

    }
}
