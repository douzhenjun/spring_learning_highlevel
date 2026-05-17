package com.tuoheng.demo04;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;

/**
 bean后处理器的作用
*/
public class Demo04 {
    public static void main(String[] args) {
        //GenericApplicationContext是一个干净的容器
        GenericApplicationContext context = new GenericApplicationContext();
        
        context.registerBean("bean1", Bean1.class);
        context.registerBean("bean2", Bean2.class);
        context.registerBean("bean3", Bean3.class);
//        context.registerBean("bean4", Bean4.class);
        
        //设置Spring容器的自动注入候选解析器, 如果不设置它, Spring框架就无法自动确定哪些Bean应该被注入到其他Bean中
        context.getDefaultListableBeanFactory().
                setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);//@Autowired, @Value
        context.registerBean(CommonAnnotationBeanPostProcessor.class);//@Resource, @PostConstructor @PreDestroy
        ConfigurationPropertiesBindingPostProcessor.register(context.getDefaultListableBeanFactory());

        context.refresh();
        System.out.println(context.getBean(Bean1.class));
        context.close();
    }
}
