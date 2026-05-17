package com.tuoheng.demo15;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/26
 **/
public class Demo15_03 {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("aaa", BeanDefinitionBuilder.genericBeanDefinition(Aspect2.class).getBeanDefinition());
        beanFactory.registerBeanDefinition("aspect1", BeanDefinitionBuilder.genericBeanDefinition(Aspect1.class).getBeanDefinition());
        AnnotationAwareAspectJAutoProxyCreator creator = new AnnotationAwareAspectJAutoProxyCreator();
        creator.setBeanFactory(beanFactory);
        Method findEligibleAdvisors = AbstractAdvisorAutoProxyCreator.class.getDeclaredMethod("findEligibleAdvisors", Class.class, String.class);
        findEligibleAdvisors.setAccessible(true);
        List obj = (List) findEligibleAdvisors.invoke(creator, Bean1.class, "bean1");
        for (Object o : obj) {
            System.out.println(o);
        }                
    }



    static class Bean1 {
        public void foo() {

        }
    }

    @Aspect
    static class Aspect1 {
        @Before("execution(* foo())")
        public void before1(){

        }
    }

    @Aspect
    static class Aspect2 {
        @Before("execution(* foo())")
        public void before2(){

        }
    }
}
