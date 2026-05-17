package com.tuoheng.demo42;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 更贴近springboot条件装配原理的演示, 相比于demo1, 需要改进以下几点:
 *  1. 需要避免条件类Condition中写死某个类的路径,这样不利于扩展
 *  2. 如果条件1和条件2互斥,那么只要定义一个条件,添加一个boolean类型的判断
 *  3. 使用自定义的ConditionalOnClass注解替代@Conditional注解
 */
public class Demo42_02 {

    public static void main(String[] args) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.registerBean("config", Config.class);
        applicationContext.registerBean(ConfigurationClassPostProcessor.class);
        applicationContext.refresh();
        
        for(String name : applicationContext.getBeanDefinitionNames()){
            System.out.println(name);
        }
    }
    
    @Configuration
    @Import(MyImportSelector.class)
    static class Config{}
    
    static class MyImportSelector implements DeferredImportSelector{
        
        @Override
        public String[] selectImports(AnnotationMetadata metadata){
            return new String[]{AutoConfiguration1.class.getName(), AutoConfiguration1.class.getName()};
        }
    }
    
    static class MyCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnClass.class.getName());
            String className = attributes.get("className").toString();
            boolean exists = (boolean)attributes.get("exists");
            boolean present = ClassUtils.isPresent(className, null);//判断某个类是否存在
            return exists ? present : !present;
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)//运行时生效
    @Target({ElementType.METHOD, ElementType.TYPE})//该注解只能作用在类上和方法头部
    @interface ConditionalOnClass{
        boolean exists();//true判断存在 false判断不存在
        
        String className();//要判断的类名
    }
    
    @Configuration
    @ConditionalOnClass(className = "com.alibaba.druid.pool.DruidDataSource", exists = false)
    static class AutoConfiguration1{
        @Bean
        public Bean1 bean1(){
            return new Bean1();
        }
    }

    @Configuration // 第三方的配置类
    @ConditionalOnClass(className = "com.alibaba.druid.pool.DruidDataSource", exists = true)
    static class AutoConfiguration2 {
        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }
    }
    
    
    static class Bean1{
        
    }
    
    static class Bean2{
        
    }
}
