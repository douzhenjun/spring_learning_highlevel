package com.tuoheng.demo05;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

public class Demo05 {
    
    private static final Logger log = LoggerFactory.getLogger(Demo05.class);

    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        
        context.registerBean("config", Config.class);
        //注册bean对象Component注解后置处理器,用来扫描@Component注解及其派生注解(如@Controller等)下的类,并注册成为Bean对象
        context.registerBean(ComponentScanPostProcessor.class);
        //注册bend对象@Bean注解后置处理器,用来扫描@Bean注解下的方法,注册其返回值类型为一个Bean对象
        context.registerBean(AtBeanPostProcessor.class);
        //注册bend对象@Mapper注解后置处理器
        context.registerBean(MapperPostProcessor.class);
        context.refresh();

        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        
        context.close();
    }
}
