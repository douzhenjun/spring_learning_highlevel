package com.tuoheng.demo20;


import com.tuoheng.demo20.config.WebConfig02;
import com.tuoheng.demo20.handler.MyRequestMappingHandlerAdapter;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class Demo20_02 {

    public static void main(String[] args) throws Exception {
        AnnotationConfigServletWebApplicationContext context = new AnnotationConfigServletWebApplicationContext
                (WebConfig02.class);
        TomcatServletWebServerFactory servletWebServerFactory = context.getBean(TomcatServletWebServerFactory.class);
        WebServer server = servletWebServerFactory.getWebServer();
        server.start();
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
        
        //1. 向/test1发起get请求
        System.out.println("向/test1发起get请求");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test1");
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        MockHttpServletResponse response = new MockHttpServletResponse();
        System.out.println(chain);
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());
        
        //2. 向/test2发起post请求
        System.out.println("向/test2发起post请求");
        request = new MockHttpServletRequest("POST", "/test2");
        request.setParameter("name", "张三");
        chain = handlerMapping.getHandler(request);
        System.out.println(chain);
        handlerAdapter.invokeHandlerMethod(request, response, (HandlerMethod) chain.getHandler());
    }
}
