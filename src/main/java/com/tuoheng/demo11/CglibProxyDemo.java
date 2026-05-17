package com.tuoheng.demo11;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class CglibProxyDemo {
    
    static class Target{
        public void foo(){
            System.out.println("target foo");
        }
    }

    public static void main(String[] param) {
        Target target = new Target();
        Target proxy = (Target) Enhancer.create(Target.class, (MethodInterceptor)(p, method, args, methodProxy) -> {
            System.out.println("before...");
            Object result = methodProxy.invoke(target, args);
//            Object result = methodProxy.invokeSuper(p, args);
            System.out.println("after...");
            return result;
        });
        
        proxy.foo();
    }
}
