package com.zhishu.common;

/**
 * 当前登录用户上下文（由鉴权拦截器写入，Controller/Service 读取）。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        CURRENT.set(userId);
    }

    /** 当前登录用户 id；未登录时触发鉴权拦截器，这里不应为 null。 */
    public static Long get() {
        return CURRENT.get();
    }

    public static Long require() {
        Long id = CURRENT.get();
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}