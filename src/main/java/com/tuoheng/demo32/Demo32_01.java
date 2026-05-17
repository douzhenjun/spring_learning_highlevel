package com.tuoheng.demo32;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 前面讲到的springmvc对异常的处理通过注解@ExceptionHandler来标记, 但它只能捕获控制器内部的异常, 如果是过滤器出现了异常
 * 是无法捕获的, 这里演示的是tomcat处理异常的原理, 将异常信息封装成json数据返回.
 * 借助了的组件有ErrorPageRegistrar和ErrorPageRegistrarBeanPostProcessor
 * 其中为了实现路径跳转还需要在报错的处理器所在的控制器内部再新建一个/error路径下的处理器方法
 */
public class Demo32_01 {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context =
                new AnnotationConfigServletWebServerApplicationContext(WebConfig01.class);
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        handlerMapping.getHandlerMethods().forEach((RequestMappingInfo k, HandlerMethod v) -> {
            System.out.println("映射路径:" + k + "\t方法信息:" + v);
        });
    }
}
