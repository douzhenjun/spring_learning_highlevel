package com.tuoheng.demo07;

import com.tuoheng.demo06.MyBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
public class Demo2 {
    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("myBean",
                BeanDefinitionBuilder.genericBeanDefinition(MyBean.class)
                        .setDestroyMethodName("destroy")
                        .getBeanDefinition());
        System.out.println(beanFactory.getBean(MyBean.class));
        beanFactory.destroySingletons();
        System.out.println(beanFactory.getBean(MyBean.class));
    }
    
    static class MyBean{
        public MyBean(){
            System.out.println("MyBean()");
        }
        
        public void destroy(){
            System.out.println("destroy");
        }
    }
}
