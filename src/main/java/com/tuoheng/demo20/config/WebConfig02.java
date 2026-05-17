package com.tuoheng.demo20.config;

import com.tuoheng.demo20.handler.MyRequestMappingHandlerAdapter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ComponentScan("com.tuoheng.demo20.controller")
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties({WebMvcProperties.class, ServerProperties.class})
public class WebConfig02 {

    //内嵌web容器工厂
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(){
        return new TomcatServletWebServerFactory(8001);
    }

    //创建DispatcherServlet
    @Bean
    public DispatcherServlet dispatcherServlet(){
        return new DispatcherServlet();
    }

    //注册DispatcherServlet, Spring Mvc的入口
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(
            DispatcherServlet dispatcherServlet, WebMvcProperties webMvcProperties){
        DispatcherServletRegistrationBean registrationBean = new DispatcherServletRegistrationBean
                (dispatcherServlet, "/");
        //初始化优先级,如果load-on-startup为正数,则表示在tomcat server启动时即初始化,否则将是有请求发来时才初始化
        registrationBean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        return registrationBean;
    }

    /**
     * 如果不注册RequestMappingHandlerMapping的Bean对象,它会使用默认的DispatcherServlet.properties中的HandlerMapping组件
     * 但这些组件只被创建作为DisaptcherServlet的成员变量, 并不能被作为Bean注册到Spring容器中供使用, 给测试带来麻烦, 为了避免, 
     * 采用@Bean方式注入RequestMapingHandlerMapping
     */
    //1.加入RequestMappingHandlerMapping
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping(){
        return new RequestMappingHandlerMapping();
    }

    //2.继续加入RequestMappingHandlerAdapter, 会替换掉DispatcherServlet默认的4个HandlerAdapter
    @Bean
    public MyRequestMappingHandlerAdapter requestMappingHandlerAdapter(){
        return new MyRequestMappingHandlerAdapter();
    }
    
}
