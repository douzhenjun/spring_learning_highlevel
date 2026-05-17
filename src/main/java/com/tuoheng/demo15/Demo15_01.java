package com.tuoheng.demo15;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * spring是如何选择代理的? 它选择通过新建一个MethodInterceptor对象, 并重写它的invoke方法
 * 该方法以MethodInvocation对象作为参数, 通过proceed()方法生成代理对象.
 * 至于如何选择代理对象是基于代理工厂对象封装的变量proxyTargetClass是否为true,以及目标类是否实现了接口
 * 详见下方注释.
 */
public class Demo15_01 {
    
    public static void main(String[] args) {

        //1.备好切点
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* foo())");
        
        //2.备好通知, MethodInterceptor来自org.aopalliance.intercept包,重写的方法是invoke(MethodInvocation)
        MethodInterceptor advice = invocation -> {
            System.out.println("before...");
            Object result = invocation.proceed();
            System.out.println("after...");
            return result;
        };
        
        //3.备好切面
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

        /*
          4.创建代理
               a.proxyTargetClass = false, 目标实现了接口, 用jdk实现
               b.proxyTargetClass = false, 目标没有实现接口, 用cglib实现
               c.proxyTargetClass = true, 总是使用cglib实现
         */
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Target1 target = new Target1();
        //创建代理, 封装目标类, 切面(advisor), 
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.addAdvisor(advisor);
        factory.setInterfaces(target.getClass().getInterfaces());
        factory.setProxyTargetClass(false);
        I1 proxy = (I1) factory.getProxy();
        System.out.println(proxy.getClass());
        proxy.foo();
        proxy.bar();

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Target2 target2 = new Target2();
        ProxyFactory factory2 = new ProxyFactory();
        factory2.setTarget(target2);
        factory2.addAdvisor(advisor);
        factory2.setInterfaces(target2.getClass().getInterfaces());
        factory2.setProxyTargetClass(false);
        Target2 proxy2 = (Target2) factory2.getProxy();
        System.out.println(proxy2.getClass());
        proxy2.foo();
        proxy2.bar();

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Target1 target3 = new Target1();
        ProxyFactory factory3 = new ProxyFactory();
        factory3.setTarget(target3);
        factory3.addAdvisor(advisor);
        factory3.setInterfaces(target3.getClass().getInterfaces());
        factory3.setProxyTargetClass(true);//默认就是true
        Target1 proxy3 = (Target1) factory3.getProxy();
        System.out.println(proxy3.getClass());
        proxy3.foo();
        proxy3.bar();
    }


    interface I1 {
        void foo();

        void bar();
    }

    static class Target1 implements I1 {
        public void foo() {
            System.out.println("target1 foo");
        }

        public void bar() {
            System.out.println("target1 bar");
        }
    }

    static class Target2 {
        public void foo() {
            System.out.println("target2 foo");
        }

        public void bar() {
            System.out.println("target2 bar");
        }
    }
}
