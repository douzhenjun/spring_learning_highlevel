package com.tuoheng.demo20;

import com.tuoheng.demo20.config.WebConfig03;
import com.tuoheng.demo20.handler.MyRequestMappingHandlerAdapter;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;

/**
 * 自定义参数解析器和返回值处理器
 */
public class Demo20_03 {

    public static void main(String[] args) throws Exception {
        AnnotationConfigServletWebApplicationContext context = new AnnotationConfigServletWebApplicationContext
                (WebConfig03.class);
        TomcatServletWebServerFactory factory = context.getBean(TomcatServletWebServerFactory.class);
        WebServer webServer = factory.getWebServer();
        webServer.start();        
        /*
          解析@RequestMapping以及派生注解, 生成路径与控制器方法的映射关系, 在初始化时就生效
          如果不注册RequestMappingHandlerMapping的Bean对象,它会使用默认的DispatcherServlet.properties中的HandlerMapping组件
          但这些组件只被创建作为DisaptcherServlet的成员变量, 并不能被作为Bean注册到Spring容器中供使用, 给测试带来麻烦, 为了避免, 
          采用@Bean方式注入RequestMapingHandlerMapping
         */
        RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
        
        /*
          新建一个自定义HandlerAdapter对象继承RequestMappingHandlerAdapter, 并在WebConfig中注入
          之所以不用默认的HandlerAdapter原因和上同
          有了处理器适配器,就可以根据HandlerMapping的处理器执行链信息去处理器对象发起请求了
         */
        MyRequestMappingHandlerAdapter handlerAdapter = context.getBean(MyRequestMappingHandlerAdapter.class);

        /*
          展示adapter自带的参数解析器和返回值处理器
         */
//        System.out.println("打印所有的参数解析器>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        for (HandlerMethodArgumentResolver argumentResolver : handlerAdapter.getArgumentResolvers()) {
//            System.out.println(argumentResolver);
//        }
//        System.out.println("打印所有的返回值处理器>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        for (HandlerMethodReturnValueHandler returnValueHandler : handlerAdapter.getReturnValueHandlers()) {
//            System.out.println(returnValueHandler);
//        }
        
        /*
          模拟发送给请求到/test3和/test4
         */
//        1. 向/test3发起put请求, 解析注解@Token并在日志中打印它所接收到的值
        System.out.println("向/test3发起put请求, 解析注解@Token并在日志中打印它所接收到的值>>>>>>>");
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/test3");
        request.addHeader("token", "令牌");
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        MockHttpServletResponse response = new MockHttpServletResponse();
        System.out.println(chain);
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());

        //2. 向/test4发起get请求, 打印yml格式的返回值
        System.out.println("向/test4发起get请求, 打印yml格式的返回值>>>>>>>");
        request = new MockHttpServletRequest("GET", "/test4");
        chain = handlerMapping.getHandler(request);
        response = new MockHttpServletResponse();
        System.out.println(chain);
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());
        //获得请求内容字节数组,转换成String类型打印出来
        byte[] contents = response.getContentAsByteArray();
        System.out.println(new String(contents, StandardCharsets.UTF_8));
        
    }
}
