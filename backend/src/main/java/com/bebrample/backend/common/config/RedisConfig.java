package com.bebrample.backend.common.config;

import com.bebrample.backend.match.Match;
import com.bebrample.backend.room.Room;
import com.bebrample.backend.room.ws.dto.MoveDto;
import com.bebrample.backend.user.entity.LobbyParticipant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Match> redisMatchTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper){
        RedisTemplate<String, Match> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> serializer = RedisSerializer.string();
        JacksonJsonRedisSerializer<Match> jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper, Match.class);
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }
    @Bean
    public RedisTemplate<String, Room> redisRoomTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper){
        RedisTemplate<String, Room> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> serializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<Room> jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper,Room.class);
        template.setKeySerializer(serializer);
        template.setValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }
    @Bean
    public RedisTemplate<String, String> redisUserTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper){
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> serializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<LobbyParticipant> jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper,LobbyParticipant.class);
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
    @Bean
    public RedisTemplate<String, MoveDto> redisMoveTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, MoveDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> serializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<MoveDto> jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper, MoveDto.class);
        template.setKeySerializer(serializer);
        template.setValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

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

        RedisCacheConfiguration historyCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)) // наприклад, сесія живе 15 хв
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));

           return RedisCacheManager.builder(redisConnectionFactory)
                   .cacheDefaults(roomCacheConfiguration)
                   .withCacheConfiguration("ROOM_CACHE", roomCacheConfiguration)
                   .withCacheConfiguration("PLAYER_SESSION_CACHE", sessionConfiguration)
                   .build();
    }
}