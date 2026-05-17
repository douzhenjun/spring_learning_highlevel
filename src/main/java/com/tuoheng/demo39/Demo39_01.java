package com.tuoheng.demo39;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

@Configuration
public class Demo39_01 {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("1. 演示获取BeanDefinition源");
        SpringApplication spring = new SpringApplication(Demo39_01.class);
        spring.setSources(Set.of("classpath:b01.xml"));
        System.out.println("2. 演示推断应用类型");
        Method deduceFromClasspath = WebApplicationType.class.getDeclaredMethod("deduceFromClasspath");
        deduceFromClasspath.setAccessible(true);
        System.out.println("\t应用类型为:"+deduceFromClasspath.invoke(null));
        System.out.println("3. 演示ApplicationContext初始化器");
        spring.addInitializers(applicationContext -> {
            if(applicationContext instanceof GenericApplicationContext gac){
                gac.registerBean("bean3", Bean3.class);
            }
        });
        System.out.println("4. 演示监听器与事件");
        spring.addListeners(event -> {
            System.out.println("\t事件为：" + event.getClass());
        });
        System.out.println("5. 演示主类推断");
        Method deduceMainApplicationClass = SpringApplication.class.getDeclaredMethod("deduceMainApplicationClass");
        deduceMainApplicationClass.setAccessible(true);
        System.out.println("\t主类是:"+deduceMainApplicationClass.invoke(spring));

        ConfigurableApplicationContext context = spring.run(args);
        
        for(String name : context.getBeanDefinitionNames()){
            System.out.println("name: "+ "来源: " + context.getBeanFactory().
                    getBeanDefinition(name).getResourceDescription());
        }
        context.close();
    }

    static class Bean1 {

    }

    static class Bean2 {

    }

    static class Bean3 {

    }

    @Bean
    public Bean2 bean2() {
        return new Bean2();
    }

    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

}
