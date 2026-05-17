package com.tuoheng.demo13;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Description methodProxy.invoke(target, params)用于代理对象执行被代理对象原方法
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class Demo2 {

    public static void main(String[] args) {
        Proxy proxy = new Proxy();
        Target target = new Target();
        proxy.setMethodInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object p, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
                System.out.println("before...");
                return methodProxy.invoke(target, params);
            }
        });

        proxy.save();
        proxy.save(2);
        proxy.save(2L);
    }
}
