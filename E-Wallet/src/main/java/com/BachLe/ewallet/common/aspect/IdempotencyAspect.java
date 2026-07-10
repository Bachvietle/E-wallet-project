package com.BachLe.ewallet.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/*
 - Khi hệ thống giám sát ngầm của Spring (có tên là AnnotationAwareAspectJAutoProxyCreator) đi
 tuần tra lúc ứng dụng vừa khởi động, nó chỉ tìm những Bean có đánh dấu @Aspect này để đứng lại phân tích.

 - Nó sẽ không gọi trực tiếp vào API đó nữa. Thay vào đó, Spring tự động tạo ra một Proxy object
 (tự động chèn thêm logic “xuyên thấu” như Transaction, Security và Logging - còn ở đây là Idempotent) - phần này đọc ở Bean Spring Boot.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    // @Around có nghĩa là hàm này sẽ "ôm trọn" cái API (chạy trước và sau API)
    // Chỉ định tọa độ - hãy đến chặn logic của bất kì nơi nào đánh dấu @Idempotent
    @Around("@annotation(com.BachLe.E_Wallet.common.annotation.Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. Lấy Request hiện tại để đọc Header
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // Lệnh này chính là để Proxy cho phép request đi tiếp vào hàm Controller gốc
        if(attributes == null){
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 2. Lấy Idempotency-Key từ Header do Client gửi lên
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if(idempotencyKey == null || idempotencyKey.trim().isEmpty()){
            throw new RuntimeException("");
        }

        // Tạo key lưu trong Redis
        String redisKey = "idemp:" + idempotencyKey;

        // 3. SETNX trong Redis (Khóa giao dịch trong 24h)
        // Lệnh này trả về TRUE nếu key chưa tồn tại (lưu thành công), FALSE nếu key đã có người chiếm
        Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", 24, TimeUnit.HOURS);


        // 4. Nếu isLockAcquired == false -> Bị trùng lặp request
        if(Boolean.FALSE.equals(isLockAcquired)){
            throw new RuntimeException("Giao dịch đang được xử lý hoặc đã hoàn thành. Vui lòng không thao tác lại!");
        }

        // 5. Nếu chiếm được khóa -> Cho phép chạy tiếp vào hàm Controller gốc
        try {
            return joinPoint.proceed();
        } catch (Exception e){

            redisTemplate.delete(redisKey);
            throw e;

        }
    }
}
