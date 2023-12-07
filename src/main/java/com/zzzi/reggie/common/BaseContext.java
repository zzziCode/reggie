package com.zzzi.reggie.common;

/**
 * @author zzzi
 * @date 2023/12/2 15:54
 * 基于ThreadLocal封装的工具类，保存当前登录的用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocale = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocale.set(id);
    }

    public static Long getCurrentId() {
        return threadLocale.get();
    }
}
