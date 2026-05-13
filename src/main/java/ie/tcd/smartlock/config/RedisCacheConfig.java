package ie.tcd.smartlock.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

/**
 * Spring Boot 会自动触发 DataRedisAutoConfiguration，
 * 默认注册 RedisConnectionFactory、RedisTemplate、StringRedisTemplate。
 * 即使不写 @Configuration 类，也能直接连上 Redis
 */
@Configuration
@EnableCaching  // 让 @Cacheable 等注解生效
public class RedisCacheConfig {
    @Value("${spring.cache.redis.time-to-live}")
    private Duration timeToLive;

    /**
     * 供 @Cacheable 等注解使用的 CacheManager 配置，翻译数据。
     * 如果不写，存入 Redis 的 Key 和 Value 就是十六进制字节码。
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 安全验证器
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("java.")               // 允许反序列化 Java 官方基础包
                .allowIfBaseType("ie.tcd.smartlock.")   // 允许反序列化自己的实体类
                .build();
        // Redis 专用的 Json 序列化配置，加上全限定类名
        // "ie.tcd.smartlock.model.vo.$AccessCodeRespMqtt",{"deviceId":"1001",...}
        ObjectMapper jsonMapper = JsonMapper.builder()
                // 通过白名单 ptv，且这个类不是 final（不是 String、boolean 等基础类），就加上类名
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                .build();

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                // Key 用 String 序列化
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // Value 用 JSON 序列化
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper))
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}
