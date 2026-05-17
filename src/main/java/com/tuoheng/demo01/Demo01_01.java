package com.tuoheng.demo01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;


/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/18
 **/
@SpringBootApplication
public class Demo01_01 {

    private static final Logger log = LoggerFactory.getLogger(Demo01_01.class);
    
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException {
        /*
         1.BeanFactory
        */
        ConfigurableApplicationContext context = SpringApplication.run(Demo01_01.class, args);
//        System.out.println(context);

        /*
         2.BeanFactory能干点啥
        */
        Field singletonObjects = DefaultSingletonBeanRegistry.class.getDeclaredField("singletonObjects");
        singletonObjects.setAccessible(true);
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        Map<String, Object> map = (Map<String, Object>) singletonObjects.get(beanFactory);
        map.entrySet().stream().filter(e -> e.getKey().startsWith("component")).forEach(e -> {
            System.out.println(e.getKey()+"="+e.getValue());
        });
        
        /*
         3. ApplicationContext比BeanFactory多点啥
        */
        //1.获得不同语言的文件
//        System.out.println(context.getMessage("hi", null, Locale.ENGLISH));
//        System.out.println(context.getMessage("hi", null, Locale.CHINESE));
//        System.out.println(context.getMessage("hi", null, Locale.JAPANESE));
        
        //2.查找资源对象所在的位置, classpath加*表示从jar包中查找, 不加*表示从类路径下找
//        Resource[] resources = context.getResources("classpath*:META-INF/spring.factories");
//        for (Resource resource : resources) {
//            System.out.println(resource);            
//        }
        
        //3.查看环境变量,查看配置文件中配置信息
//        System.out.println(context.getEnvironment().getProperty("java_home"));
//        System.out.println(context.getEnvironment().getProperty("server.port"));
        
        //4.发送事件,ComponentB负责监听并处理(打印控制台日志)
//        context.publishEvent(new UserRegisteredEvent(context));
        context.getBean(ComponentA.class).register();
    }
    
}
