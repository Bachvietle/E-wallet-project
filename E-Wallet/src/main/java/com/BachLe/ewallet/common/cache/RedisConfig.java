package com.BachLe.ewallet.common.cache;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // RateTemplate giống như kiểu Manager Redis - cầu nối giữa Spring với Redis Sever

    // Cấu hình Serializer/ Deserializer cho RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Sử dụng String cho Key để dễ đọc
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Sử dụng JSON cho Value để lưu Object dễ dàng
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }

    // Cấu hình Serializer/ Deserializer cho SpringCache(@Cacheable, @CacheEvict)
    // Các annotation như @Cacheable, @CacheEvict không dùng RedisTemplate. Chúng sử dụng một Bean khác có tên là RedisCacheManager
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. Tạo Serializer JSON giống hệt với RedisTemplate
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 2. Cấu hình mặc định cho Cache
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // Không lưu giá trị null để tiết kiệm RAM
                .disableCachingNullValues()
                // (Tùy chọn) Cài đặt thời gian sống mặc định của Cache (VD: 1 tiếng)
                .entryTtl(Duration.ofMinutes(60))
                // Cấu hình Serializer cho Key là String
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Cấu hình Serializer cho Value là JSON
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // 3. Build và trả về CacheManager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
