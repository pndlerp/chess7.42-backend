package com.bebrample.backend.common.config;

import com.bebrample.backend.room.Room;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){
           RedisCacheConfiguration roomCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new JacksonJsonRedisSerializer<Room>(Room.class)));

           RedisCacheConfiguration sessionConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)) // наприклад, сесія живе 15 хв
                .disableCachingNullValues()
                // Ключі — рядки
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                // Значення — ТЕЖ чисті рядки String (ідеально для простого ID кімнати)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));

           return RedisCacheManager.builder(redisConnectionFactory)
                   .cacheDefaults(roomCacheConfiguration)
                   .withCacheConfiguration("PLAYER_SESSION_CACHE", sessionConfiguration)
                   .build();
    }
}