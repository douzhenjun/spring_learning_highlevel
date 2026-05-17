package com.tuoheng.demo39;

import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Demo39_02 {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        //添加app监听器
        SpringApplication app = new SpringApplication();
        app.addListeners(e -> System.out.println(e.getClass()));
        
        //获取事件发送器实现类名
        List<String> names = SpringFactoriesLoader.loadFactoryNames(SpringApplicationRunListener.class, Demo39_02.class.getClassLoader());
        for (String name : names) {
            System.out.println(name);
            Class<?> clazz = Class.forName(name);
            Constructor<?> constructor = clazz.getConstructor(SpringApplication.class, String[].class);
            //事件发布器
            SpringApplicationRunListener publisher = (SpringApplicationRunListener) constructor.newInstance(app, args);
            
            /*
             * 发布事件
             */
            DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
            //springboot开始启动
            publisher.starting(bootstrapContext);
            //环境信息准备完毕
            publisher.environmentPrepared(bootstrapContext, new StandardEnvironment());
            //在Spring容器创建, 并调用初始化之后, 发送此事件
            GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
            publisher.contextPrepared(genericApplicationContext);
            //所有BeanDefinition加载完毕
            publisher.contextLoaded(genericApplicationContext);
            genericApplicationContext.refresh();
            //spring容器初始化完成
            publisher.started(genericApplicationContext);
            //springboot启动完毕
            publisher.running(genericApplicationContext);
            //springboot启动出错了
            publisher.failed(genericApplicationContext, new Exception("出错了"));
        }
    }
}
