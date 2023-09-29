package com.ps.utils;

public class RedisConstants {
    public static final Long CACHE_GOODS_TTL=30L;
    public static final Long CACHE_NULL_TTL=2L;
    public static final String CACHE_GOODS_KEY="cache:goods:";
    public static final String LOCK_GOODS_KEY="lock:goods:";
    public static final Long LOCK_GOODS_TTL=10L;
}
