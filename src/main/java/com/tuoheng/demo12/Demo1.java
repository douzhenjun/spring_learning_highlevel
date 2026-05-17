package com.tuoheng.demo12;

/**
 * @Description 模拟jdk代理的实现
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class Demo1 {

    interface Foo {
        void foo();

        int bar();
    }

    static class Target implements Foo {
        public void foo() {
            System.out.println("target foo");
        }

        @Override
        public int bar() {
            System.out.println("target bar");
            return 100;
        }
    }

    public static void main(String[] param) {
        Foo proxy = new $Proxy0((proxy1, method, args) -> {
            System.out.println("before");
            return method.invoke(new Target(), args);
        });
        proxy.foo();
        proxy.bar();
    }
}
