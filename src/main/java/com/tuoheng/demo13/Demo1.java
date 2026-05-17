package com.tuoheng.demo13;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Description cglib通过继承目标类重写方法的方式实现代理,
 *              对于重写的方法,通过org.springframework.cglib.proxy.MethodInterceptor
 *              调用intercept方法,传入以下参数:1.代理类对象2.增强方法对象3.参数列表(无参数定义一个空列表)
 *              4.org.springframework.cglib.proxy.MethodProxy对象
 *              intercept方法拦截被代理对象的某个方法,并增加增强的代码.
 *              invokeSuper用于代理对象执行被代理对象原方法
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class Demo1 {
    public static void main(String[] args) {
        Proxy proxy = new Proxy();
        proxy.setMethodInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object p, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
                System.out.println("before...");
                return methodProxy.invokeSuper(p, params);
            }
        });
        
        proxy.save();
        proxy.save(2);
        proxy.save(2L);
    }
}
