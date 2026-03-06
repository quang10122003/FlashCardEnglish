package com.TestFlashCard.FlashCard.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // TTL mặc định cho các cache khác (giữ 30 phút như bạn đang dùng)
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // prefix để gom nhóm key trong Redis (dễ xóa/quan sát)
                .computePrefixWith(cacheName -> "vocalearn::" + cacheName + "::");

        // TTL riêng cho cache presignedUrls = 23 giờ (luôn < 24h presign)
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("presignedUrls", defaults.entryTtl(Duration.ofHours(23)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    @Bean
    public SimpleCacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
