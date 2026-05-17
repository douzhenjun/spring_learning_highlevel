package com.tuoheng.demo16;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;

/**
 * @Description 切点匹配的原理, 介绍两种常用的, 根据表达式匹配和根据是否添加了某个注解
 *              这个demo有两个目的, 第一, 展示两种切点匹配方式的效果; 第二, 演示注解匹配时, 对三种情况进行了匹配, 但这里
 *              只能匹配直接注解在方法头部上的情况. 解决方式在demo2中
 * @Author douzhenjun
 * @DATE 2023/4/26
 **/
public class Demo16_01 {

    public static void main(String[] args) throws NoSuchMethodException {
        /*
          1.对比切点表达式execution和@annotation匹配
          execution([方法的可见性] 返回类型 [方法所在类的全路径名] 方法名(参数类型列表) [方法抛出的异常类型])
         */
        System.out.println("1.对比切点表达式execution和@annotation匹配>>>>>>>>>>>>>>>>>");
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        //这表示匹配所有方法名为bar且无参数的方法
        pointcut.setExpression("execution(* bar())");
        System.out.println(pointcut.matches(ClassDemos.T1.class.getMethod("foo"), ClassDemos.T1.class));//false
        System.out.println(pointcut.matches(ClassDemos.T1.class.getMethod("bar"), ClassDemos.T1.class));//true
        
        AspectJExpressionPointcut pointcut2 = new AspectJExpressionPointcut();
        //这表示匹配所有被@Transactional注解的方法
        pointcut2.setExpression("@annotation(org.springframework.transaction.annotation.Transactional)");
        System.out.println(pointcut2.matches(ClassDemos.T1.class.getMethod("foo"), ClassDemos.T1.class));//true
        System.out.println(pointcut2.matches(ClassDemos.T1.class.getMethod("bar"), ClassDemos.T1.class));//false

        /*
          2.比较注解在方法上,注解在类上,以及注解在类实现的接口上,这些切点能否匹配到
         */
        System.out.println("2.比较注解在方法上,注解在类上,以及注解在类实现的接口上,这些切点能否匹配到>>>>>>>>>>>>>>>>>");
        AspectJExpressionPointcut pointcut3 = new AspectJExpressionPointcut();
        pointcut3.setExpression("@annotation(org.springframework.transaction.annotation.Transactional)");
        System.out.println(pointcut3.matches(ClassDemos.T1.class.getMethod("foo"), ClassDemos.T1.class));//true
        System.out.println(pointcut3.matches(ClassDemos.T2.class.getMethod("foo"), ClassDemos.T2.class));//false
        System.out.println(pointcut3.matches(ClassDemos.T3.class.getMethod("foo"), ClassDemos.T3.class));//false
    }
    
}
