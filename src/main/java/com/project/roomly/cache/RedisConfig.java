package com.project.roomly.cache;

import com.project.roomly.dto.Media.ResponseHotelMediaDto;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory){

        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();


        redisCacheConfigurationMap.put("HOTEL", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(ResponseHotelMediaDto.class))));

        redisCacheConfigurationMap.put("ROOM", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(ResponseRoomMediaDto.class))));


        redisCacheConfigurationMap.put("CHECK_OWNER_ROOM", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(24)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Boolean.class))));

        redisCacheConfigurationMap.put("CHECK_OWNER_HOTEL", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Boolean.class))));

        return RedisCacheManager.builder(redisConnectionFactory)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .build();
    }
}
