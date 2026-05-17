package com.tuoheng.demo33;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

/**
 * 演示浏览器访问路径,而对应路径下的处理器方法被执行的现象, 注意到这里并非是使用了@Controller注解的类, 所有的类都是实现了
 * Controller这个接口,而以他们的bean名称作为访问路径的.
 * 生效的组件是bean对象BeanNameUrlHandlerMapping和SimpleControllerHandlerAdapter
 */
public class Demo33_01 {
    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext(WebConfig01.class);
    }
}
