package com.tuoheng.demo06;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 Aware接口及Initializing接口
*/
public class Demo06 {
    
    private static final Logger log = LoggerFactory.getLogger(Demo06.class);

    public static void main(String[] args) {

        GenericApplicationContext context = new GenericApplicationContext();
        //向容器中注入名称为myBean的MyBean对象
        context.registerBean("myBean", MyBean.class);
//        context.registerBean("myConfig1", MyConfig1.class);//注入MyConfig1对象,注解方法未执行,理由是没有注入Bean对象后置处理器
//        context.registerBean("myConfig2", MyConfig2.class);
        context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
        context.registerBean(CommonAnnotationBeanPostProcessor.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();//1.执行beanFactory后处理器; 2.添加bean的后处理器; 3.创建和初始化单例.
        context.close();
    }
}


