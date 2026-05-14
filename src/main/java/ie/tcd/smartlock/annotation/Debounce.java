package ie.tcd.smartlock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Controller 方法走自定义防抖规则，覆盖默认 500ms 短锁。
 * 注意：通过 Spring 代理生效，类内部直接调用不会触发。
 */
//元注解 Meta-Annotations
@Target(ElementType.METHOD)         // 注解能贴在哪里 (METHOD)
@Retention(RetentionPolicy.RUNTIME) // 注解能活多久 (AOP 必选 RUNTIME)
public @interface Debounce {

    /**
     * 锁的 TTL，默认 500 毫秒。需大于业务最坏耗时，避免业务未结束锁就过期产生误删。
     */
    long timeout() default 500L;

    /**
     * 业务正常返回后是否立即删 key。异常时永不释放，靠 TTL 兜底。
     */
    boolean releaseOnSuccess() default false;

    /**
     * 可选 SpEL，参与 key 计算。例如 "#request.deviceId" 实现按设备分锁。
     */
    String key() default "";
}
