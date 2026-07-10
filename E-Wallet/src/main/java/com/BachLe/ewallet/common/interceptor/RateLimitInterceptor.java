package com.BachLe.ewallet.common.interceptor;

import com.BachLe.ewallet.common.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor

/*
Khi một Request đi vào hệ thống, Spring sẽ tìm ra "Handler" nào sẽ xử lý nó.
Nếu Handler đó là một method trong một @Controller, Spring sẽ gói nó vào một đối tượng gọi là HandlerMethod.
 */

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Chỉ xử lý nếu method handle là 1 method controller
        if(!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }

        // 2. Ktra xem method hay class đó có chứa @RateLimit hay ko

        // tìm trên hàm (method)
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            // nếu ko có trên hàm thì tìm trên class
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        // nếu trên class cx ko có thì cho qua
        if (rateLimit == null) {
            return true;
        }

        // 3. Bắt đầu logic rate limit (sau khi đã qua được Proxy/Load Balancer)
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // Tạo redisKey
        String redisKey = "rate_limit:" + clientIp + ":" + requestUri;

        // 4. Thuật toán Fixed Window bằng Redis INCR
        // Hàm increment() tự động tăng giá trị lên 1. Nếu key chưa có, nó khởi tạo là 1.
        Long count = redisTemplate.opsForValue().increment(redisKey);


        // 5. Nếu đây là request đầu tiên trong chu kì, set thời gian hết hạn
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, rateLimit.duration(), TimeUnit.SECONDS);
        }


        // 6. Ktra vượt ngưỡng
        if(count != null && count > rateLimit.requests()){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Bạn thao tác quá nhanh, vui lòng thử lại sau!\"}");
            return false; // Chặn request lại, KHÔNG cho vào Controller
        }

        return true; // Cho phép đi tiếp
    }

    // Helper lấy IP thực sự
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
