package com.tuoheng.demo20;

import com.tuoheng.demo20.config.WebConfig01;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * handlerMapping.getHandlerMethods
 */
public class Demo20_01 {
    
    private static final Logger log = LoggerFactory.getLogger(Demo20_01.class);

    public static void main(String[] args) throws Exception {
        AnnotationConfigServletWebApplicationContext context = new AnnotationConfigServletWebApplicationContext(WebConfig01.class);
        TomcatServletWebServerFactory servletWebServerFactory = context.getBean(TomcatServletWebServerFactory.class);
        WebServer server = servletWebServerFactory.getWebServer();
        server.start();
        //解析@RequestMapping以及派生注解, 生成路径于控制器方法的映射关系, 在初始化时就生效
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);

        //获取映射的结果, 比如 {POST [/test2]}=com.tuoheng.demo20.Controller1#test2(String)
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        handlerMethods.forEach((k, v) -> {
            System.out.println(k+"="+v);
        });
    }
}
