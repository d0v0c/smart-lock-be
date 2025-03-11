package ie.tcd.scss.smartdoorlockbe.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.TimeZone;

/**
 * @author xylingying
 * @date 2025-03-07 17:30
 * @description: redis配置
 */
@Configuration
@EnableCaching  //开启缓存支持
public class RedisCacheConfig {
    @Value("${spring.cache.redis.time-to-live}")
    private Duration timeToLive;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {return new LettuceConnectionFactory();}

    // 配置Java对象序列化到redis的方式为JSON
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()).setTimeZone(TimeZone.getDefault());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper))
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

//    @Bean
//    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
//        // 注册对 Java 8 日期时间类型的支持（包括 ZonedDateTime）
//        objectMapper.registerModule(new JavaTimeModule());
//        // 可根据需求设置其他 ObjectMapper 配置项，例如关闭写时间戳
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(
//                                new GenericJackson2JsonRedisSerializer(objectMapper)
//                        )
//                );
//    }
}
