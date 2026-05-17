package com.tuoheng.demo11;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class JdkProxyDemo {
    
    interface Foo{
        void foo();
    }
    
    static final class Target implements Foo{
        public void foo(){
            System.out.println("target foo");
        }
    }

    public static void main(String[] param) throws IOException {
        //目标对象
        Target target = new Target();
        
        ClassLoader classLoader = JdkProxyDemo.class.getClassLoader();
        Foo proxy = (Foo) Proxy.newProxyInstance(classLoader, new Class[]{Foo.class}, (p, method, args) ->{
            System.out.println("before...");
            Object result = method.invoke(target, args);
            System.out.println(result);
            System.out.println("after...");
            return result;
        });

        System.out.println(proxy.getClass());
        proxy.foo();
        System.in.read();
    }
}
