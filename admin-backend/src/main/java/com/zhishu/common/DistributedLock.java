package com.zhishu.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁（Redis）：SET key token NX PX ttl 加锁，Lua 脚本校验 token 后释放，防误删他人锁。
 * 用于 admin 直连主库写操作，与 backend 并发读写共存时防冲突。
 */
@Component
public class DistributedLock {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;

    public DistributedLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 尝试加锁，返回唯一 token；拿不到锁返回 null。 */
    public String tryLock(String key, long ttlMs) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttlMs, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    /** 释放锁（校验 token，防误删）。 */
    public void unlock(String key, String token) {
        redis.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
    }

    /** 加锁并执行，超时拿不到锁抛业务异常。 */
    public <T> T withLock(String key, long ttlMs, LockAction<T> action) {
        String token = tryLock(key, ttlMs);
        if (token == null) {
            throw new BusinessException(409, "操作冲突，请稍后重试");
        }
        try {
            return action.run();
        } finally {
            unlock(key, token);
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {
        T run();
    }
}