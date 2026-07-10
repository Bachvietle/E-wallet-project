package com.BachLe.ewallet.domain.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerifyRegisterMail(String verifyLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Xác minh tài khoản");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để xác minh tài khoản:" +
                    "<a href=\"" + verifyLink + "\">Xác minh tài khoản</a>", true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new MessagingException();
        }
    }


    public void sendResetPwMail(String resetPwLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Đặt lại mật khẩu");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để đặt lại mật khẩu:" +
                    "<a href=\"" + resetPwLink + "\">Đặt lại mật khẩu</a>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Ko gửi được link xác minh");
        }
    }
}
