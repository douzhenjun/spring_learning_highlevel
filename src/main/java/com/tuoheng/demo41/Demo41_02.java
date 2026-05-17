package com.tuoheng.demo41;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

public class Demo41_02 {

    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new SimpleCommandLinePropertySource(
                "--spring.datasource.url=jdbc:mysql://localhost:3306/test",
                "--spring.datasource.username=root",
                "--spring.datasource.password=root"
        ));
        context.setEnvironment(env);
        context.registerBean("config", Config.class);
        context.refresh();

        for (String beanDefinitionName : context.getBeanDefinitionNames()) {
            String resourceDescription = context.getBeanDefinition(beanDefinitionName).getResourceDescription();
            if(resourceDescription != null){
                System.out.println(beanDefinitionName+"来源:"+resourceDescription);
            }
            context.close();
        }
    }
    
    
    static class Config{
        @Bean
        public TomcatServletWebServerFactory tomcatServletWebServerFactory(){
            return new TomcatServletWebServerFactory();
        }
    }
    
    @Configuration
    static class AutoConfiguration1{
        @Bean
        public Bean1 bean1(){
            return new Bean1();
        }
    }
    
    @Configuration
    static class AutoConfiguration2{
        @Bean
        public Bean2 bean2(){
            return new Bean2();
        }
    }
    
    static class Bean1{}
    
    static class Bean2{}
}
