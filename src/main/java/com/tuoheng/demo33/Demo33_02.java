package com.tuoheng.demo33;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

/**
 * 这里我们通过自定义一些组件来模拟第一个例子中BeanNameUrlHandlerMapping和SimpleControllerHandlerAdapter
 * 处理请求映射和处理器执行链的过程.
 * 1. 首先自定义MyHandlerMapping实现HandlerMapping接口的getHandler方法和定义初始化方法init.
 * getHandler方法就是根据请求的路径uri从map对象collect中获得对应的值( collect的初始化在init方法中,
 * 它将过滤一切以"/"开头的字符串作为bean名称的bean对象), 并且封装成一个处理器执行链, 返回给处理器适配器
 * 2. 然后自定义处理器适配器实现HandlerAdapter接口, 重写两个方法support和handle, 第一个方法用来判断处理器对象是否
 * 实现了Controller接口,返回一个布尔值; 第二个方法通过supports方法返回的值为true,调用其handleRequest方法,这个方法
 * 就是用来根据执行链信息找到对应的处理器方法并执行之.
 */
public class Demo33_02 {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext(WebConfig02.class);
    }
}
