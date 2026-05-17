package com.tuoheng.demo42;

import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * 这个案例主要演示如何实现条件装配, 条件装配指的是待装配的第三方配置类满足一定的条件才能生效
 * 步骤如下:
 *  1.首先定义一个自定义的配置类, 该类将通过@Import注解导入一个ImportSelector的实现类
 *  2.自定义一个MyImportSelector类实现ImportSelector(或DeferredImportSelector接口),重写它的selectImports方法，
 *    该方法将返回需要引入的第三方配置类
 *  3. 定义两个第三方配置类，在配置类的上方添加@Conditional注解，该注解的内容是一个条件类
 *  4. 定义条件类实现Condition接口，重写里面的matches方法，该方法返回一个boolean型，告诉第三方配置类只有在某个类存在的前提下才能生效
 *      或者只有在某个类不存在的前提下才能生效
 *  5. 主方法测试
 */
public class Demo42_01 {

    public static void main(String[] args) {

        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();
        
        for(String name : context.getBeanDefinitionNames()){
            System.out.println(name);
        }
    }
    
    //本项目的配置类
    @Configuration
    @Import(MyImportSelector.class)
    static class Config{}
    
    //封装两个第三方自动配置类返回
    static class MyImportSelector implements DeferredImportSelector{
        @Override
        public String[] selectImports(AnnotationMetadata metadata){
            return new String[]{AutoConfiguration1.class.getName(), AutoConfiguration2.class.getName()};
        }
    }

    //定义条件1
    static class MyCondition1 implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata){
            return ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
        }
    }
    
    //定义条件2
    static class MyCondition2 implements Condition{
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata){
            return !ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
        }
    }
    
    @Configuration // 第三方的配置类
    @Conditional(MyCondition1.class)//配置类满足这个注解的条件时才生效, 下同
    static class AutoConfiguration1 {
        @Bean
        public Bean1 bean1() {
            return new Bean1();
        }
    }

    @Configuration // 第三方的配置类
    @Conditional(MyCondition2.class)
    static class AutoConfiguration2 {
        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }
    }

    static class Bean1 {

    }

    static class Bean2 {

    }
}
