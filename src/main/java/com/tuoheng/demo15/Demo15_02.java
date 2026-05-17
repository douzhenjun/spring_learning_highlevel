package com.tuoheng.demo15;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/26
 **/
public class Demo15_02 {

    public static void main(String[] args) {

        GenericApplicationContext context = new GenericApplicationContext();
        //注入配置类对象
        context.registerBean("myconfig", MyConfig.class);
        //注入后置处理器对象
        context.registerBean(ConfigurationClassPostProcessor.class);
        //注入切面类对象(高级切面@Aspect)
        context.registerBean("aspect1", Aspect1.class);
        //注入根据切面创建代理对象的核心类对象
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        context.refresh();
        
        Bean1 bean1 = context.getBean(Bean1.class);
        bean1.foo();
        System.out.println(bean1.getClass());
    }

    static class MyConfig{
        @Bean
        @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        public Bean1 bean1(){
            return new Bean1();
        }
    }

    static class Bean1 {
        public void foo() {
            System.out.println("bean1 foo");
        }
    }
    
    @Aspect
    static class Aspect1{
        @Around("execution(* foo())")
        public Object before(ProceedingJoinPoint pjp) throws Throwable {
            System.out.println("aspect1 around");
            return pjp.proceed();
        }
    }
}
