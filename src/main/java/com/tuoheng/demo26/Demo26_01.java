package com.tuoheng.demo26;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExpressionValueMethodArgumentResolver;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Demo26_01 {
    public static void main(String[] args) throws Exception {
        /*
         * 定义注解配置上下文对象和请求映射处理器适配器对象, 并完成bean注入和初始化
         */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
        RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.setApplicationContext(context);
        adapter.afterPropertiesSet();

        /*
         * 定义一个模拟请求,封装参数信息
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("name", "张三");


        /*
         * 现在可以通过 ServletInvocableHandlerMethod 把这些整合在一起, 并完成控制器方法的调用, 如下
         */
        ServletInvocableHandlerMethod handlerMethod = new ServletInvocableHandlerMethod(
                new WebConfig.Controller1(), 
                WebConfig.Controller1.class.getMethod("foo", WebConfig.User.class)
        );
        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(
                null, null);
        handlerMethod.setDataBinderFactory(factory);
        handlerMethod.setParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
        handlerMethod.setHandlerMethodArgumentResolvers(getArgumentResolvers(context));

        /*
         * 获取模型工厂方法, 初始化模型数据, 调用者handlerAdapter对象, 传入参数ServletInvocableHandlerMethod对象
         * (封装流程中与handlerAdapter对象交互的三个对象)和ServletRequestDataBinderFactory对象.
         */
        ModelAndViewContainer container = new ModelAndViewContainer();
        Method getMethodFactory = RequestMappingHandlerAdapter.class.getDeclaredMethod(
                "getModelFactory", HandlerMethod.class, WebDataBinderFactory.class);
        getMethodFactory.setAccessible(true);
        ModelFactory modelFactory = (ModelFactory) getMethodFactory.invoke(adapter, handlerMethod, factory);
        
        //初始化模型数据, initModel方法用于扫描所有注解了@ModelAttribute的控制器方法, 并以key-value
        //形式保存到ModelAndViewContainer对象中
        modelFactory.initModel(new ServletWebRequest(request), container, handlerMethod);
        handlerMethod.invokeAndHandle(new ServletWebRequest(request), container);
        System.out.println(container.getModel());
        
        //关闭context
        context.close();
    }

    public static HandlerMethodArgumentResolverComposite getArgumentResolvers(AnnotationConfigApplicationContext context) {
        HandlerMethodArgumentResolverComposite composite = new HandlerMethodArgumentResolverComposite();
        composite.addResolvers(
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), false),
                new PathVariableMethodArgumentResolver(),
                new RequestHeaderMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletCookieValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ExpressionValueMethodArgumentResolver(context.getDefaultListableBeanFactory()),
                new ServletRequestMethodArgumentResolver(),
                new ServletModelAttributeMethodProcessor(false),
                new RequestResponseBodyMethodProcessor(List.of(new MappingJackson2HttpMessageConverter())),
                new ServletModelAttributeMethodProcessor(true),
                new RequestParamMethodArgumentResolver(context.getDefaultListableBeanFactory(), true)
        );
        return composite;
    }
}
