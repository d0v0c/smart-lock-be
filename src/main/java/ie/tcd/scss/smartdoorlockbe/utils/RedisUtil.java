//package ie.tcd.scss.smartdoorlockbe.utils;
//
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.Resource;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class RedisUtil {
//    @Resource
//    private RedisTemplate redisTemplate;
//
//    /**
//     * 判断key是否存在
//     *
//     * @param key
//     * @return
//     */
//    public boolean hasKey(String key) {
//        if (StringUtils.hasLength(key)) {
//            return redisTemplate.hasKey(key);
//        }
//        return false;
//    }
//
//    /**
//     * 删除key
//     *
//     * @param key
//     * @return
//     */
//    public boolean delete(String key) {
//        if (StringUtils.hasLength(key)) {
//            return redisTemplate.delete(key);
//        }
//        return false;
//    }
//
//    /**
//     * 设置key过期时间
//     *
//     * @param key
//     * @param timeout
//     * @param timeUnit
//     * @return
//     */
//    public boolean expTime(String key, long timeout, TimeUnit timeUnit) {
//        if (StringUtils.hasLength(key)) {
//            return redisTemplate.expire(key, timeout, timeUnit);
//        }
//        return false;
//    }
//
//    public <T> void set(String key, T value) {
//        if (StringUtils.hasLength(key)) {
//            redisTemplate.opsForValue().set(key, value);
//        }
//    }
//
//    public <T> T get(String key) {
//        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
//        return valueOperations.get(key);
//    }
//}
