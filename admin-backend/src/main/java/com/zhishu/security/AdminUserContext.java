package com.zhishu.security;

public final class AdminUserContext {
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private AdminUserContext() {
    }

    public static void set(Long adminId) {
        CURRENT.set(adminId);
    }

    public static Long get() {
        return CURRENT.get();
    }

    public static Long require() {
        Long id = CURRENT.get();
        if (id == null) {
            throw new com.zhishu.common.BusinessException(401, "请先登录");
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}