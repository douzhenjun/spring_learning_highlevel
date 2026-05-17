package com.tuoheng.demo12;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @Description jdk proxy创建代理的方式
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class $Proxy0 extends Proxy implements Demo1.Foo {
    
    static Method foo;
    static Method bar;
    
    static{
        try {
            foo = Demo1.Foo.class.getMethod("foo");
            bar = Demo1.Foo.class.getMethod("bar");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    public $Proxy0(InvocationHandler h){
        super(h);
    }
    
    @Override
    public void foo() {
        try {
            h.invoke(this, foo, new Object[0]);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public int bar() {
        try {
            Object result = h.invoke(this, bar, new Object[0]);
            return (int)result;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new UndeclaredThrowableException(e);
        }
    }
}
