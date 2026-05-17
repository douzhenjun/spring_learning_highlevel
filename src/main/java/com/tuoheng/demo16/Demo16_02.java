package com.tuoheng.demo16;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * @Description 模拟spring对注解匹配的三种情况进行匹配的原理实现, 这三种情况分别是:
 *                  a.直接注解在方法上
 *                  b.注解在方法所在的类上
 *                  c.注解在方法所在的类所继承或实现的类或接口上
 * @Author douzhenjun
 * @DATE 2023/4/26
 **/
public class Demo16_02 {

    public static void main(String[] args) throws NoSuchMethodException {
        StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                //当注解直接在方法上时, 匹配为true
                MergedAnnotations annotations = MergedAnnotations.from(method);
                if(annotations.isPresent(Transactional.class)){
                    return true;
                }
                //当注解注解在目标类或它的父类上时, 匹配为true
                //MergedAnnotations.SearchStrategy搜寻策略默认是DIRECT, 这里改成TYPE_HIERARCHY是为了匹配第三种情况
                annotations = MergedAnnotations.from(targetClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
                if(annotations.isPresent(Transactional.class)){
                    return true;
                }
                return false;
            }
        };

        System.out.println(pointcut.matches(ClassDemos.T1.class.getMethod("foo"), ClassDemos.T1.class));
        System.out.println(pointcut.matches(ClassDemos.T2.class.getMethod("foo"), ClassDemos.T2.class));
        System.out.println(pointcut.matches(ClassDemos.T3.class.getMethod("foo"), ClassDemos.T3.class));
    }
}
