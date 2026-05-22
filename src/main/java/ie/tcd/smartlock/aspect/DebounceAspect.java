package ie.tcd.smartlock.aspect;

import ie.tcd.smartlock.utils.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;

/**
 * 防抖切面：拦截所有 Controller，按 user+method+uri[+SpEL key] 在 Redis 上 SETNX。
 * 默认 500ms TTL；@Debounce 可覆盖。GET/HEAD/OPTIONS 跳过。
 */
@Aspect
@Component
@Slf4j
public class DebounceAspect {

    private static final long DEFAULT_TTL_MS = 500L;
    private static final Set<String> SKIP_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final String KEY_PREFIX = "debounce:";

    private final StringRedisTemplate redis;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    public DebounceAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // 拦截函数执行 任意返回类型 controller包 所有子包 所有类 所有方法 任意参数
    // execution   *  smartlock.controller  ..     *      .*     (..)
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // HTTP 上下文
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 非 HTTP 请求直接放行
        if (attrs == null) return pjp.proceed();
        // GET/HEAD/OPTIONS 直接放行
        HttpServletRequest request = attrs.getRequest();
        String httpMethod = request.getMethod();
        if (SKIP_METHODS.contains(httpMethod)) return pjp.proceed();

        // 解析注解参数
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        // SpEL 1) 拿到函数
        Method method = sig.getMethod();
        Debounce ann = method.getAnnotation(Debounce.class);
        long ttl = DEFAULT_TTL_MS;
        boolean releaseOnSuccess = false;
        String keyExpr = "";
        if (ann != null) {
            ttl = ann.timeout();
            releaseOnSuccess = ann.releaseOnSuccess();
            keyExpr = ann.key();
        }

        // Redis的 key = KEY_PREFIX + principal + ":" + httpMethod + ":" + uri + extra;
        // 例如： debounce:alice:POST:/api/code:10001
        String principal = resolvePrincipal(request);
        String uri = request.getRequestURI();
        String extra = "";
        if (!keyExpr.isEmpty()) {
            try {
                Object val = evaluate(keyExpr, method, pjp.getArgs());
                extra = ":" + val;
            } catch (Exception e) {
                log.warn("Debounce SpEL eval failed [{}]: {}", keyExpr, e.getMessage());
            }
        }
        String key = KEY_PREFIX + principal + ":" + httpMethod + ":" + uri + extra;

        Boolean acquired;
        try {
            // 加锁，SETNX， SET if Not eXists
            acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofMillis(ttl));
        } catch (Exception e) {
            log.warn("Debounce Redis unavailable, passing through: {}", e.getMessage());
            return pjp.proceed();
        }
        // 没加到锁，那就不能执行
        if (!Boolean.TRUE.equals(acquired)) {
            throw new TooManyRequestsException();
        }

        // 执行业务代码
        Object result = pjp.proceed();

        // 释放锁
        if (releaseOnSuccess) {
            try {
                redis.delete(key);
            } catch (Exception e) {
                log.warn("Debounce release failed [{}]: {}", key, e.getMessage());
            }
        }
        return result;
    }

    // 解析出 username / IP
    private String resolvePrincipal(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 有 username
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        // nginx 反代拿 IP
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        // 直接拿 IP
        return request.getRemoteAddr();
    }

    //  SpEL 求值，拿到设备 ID
    // expr = "#request.deviceId";
    // method = generateCode;
    // args = [AccessCodeGenerateReqVO 实例, Authentication 实例];
    private Object evaluate(String expr, Method method, Object[] args) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        // SpEL 2) 拿到参数名
        // names = ["request", "authentication"]
        String[] names = paramDiscoverer.getParameterNames(method);
        if (names != null) {
            for (int i = 0; i < names.length && i < args.length; i++) {
                // SpEL 3) 参数名 -- 参数实例 字典
                // "request" AccessCodeGenerateReqVO 实例
                // "authentication" Authentication 实例
                ctx.setVariable(names[i], args[i]);
            }
        }
        // SpEL 4) 编译表达式
        // "#request" ".deviceId"
        Expression expression = parser.parseExpression(expr);
        // SpEL 5) 根据字典运行表达式
        // "#request" -> ctx("request") -> AccessCodeGenerateReqVO 实例
        // ".deviceId" -> AccessCodeGenerateReqVO.getDeviceId()
        return expression.getValue(ctx);
    }
}
