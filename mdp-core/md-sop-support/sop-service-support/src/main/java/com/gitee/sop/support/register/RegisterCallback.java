package com.gitee.sop.support.register;

import com.gitee.sop.support.annotation.Open;

import java.lang.reflect.Method;

@FunctionalInterface
public interface RegisterCallback {
    void callback(Class<?> interfaceClass, Method method, Open open);
}
