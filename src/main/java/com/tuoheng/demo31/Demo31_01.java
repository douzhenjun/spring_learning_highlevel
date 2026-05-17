package com.tuoheng.demo31;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.nio.charset.StandardCharsets;

/**
 * 上个案例说的是具体的控制器中扫描@ExceptionHandler注解的方法, 当调用该控制器下的其他方法爆出指定异常时
 * 会把异常的处理交给@ExceptionHandler注解的方法. 如果说在某个Controller下并没有用@ExceptionHandler注解的方法,
 * 并且调用了该Controller下的某个处理方法爆出了异常, 则它会查找@ControllerAdvice注解的类中有没有@ExceptionHandler注解
 * 的方法, 以及有没有合适的异常类型与之匹配, 这是全局的异常处理方法调用, 优先级低于局部异常处理方法调用.
 */
public class Demo31_01 {
    public static void main(String[] args) throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
        ExceptionHandlerExceptionResolver resolver = context.getBean(ExceptionHandlerExceptionResolver.class);

        HandlerMethod handlerMethod = new HandlerMethod(new Controller5(), Controller5.class.getMethod("foo"));
        Exception e = new Exception("e1");
        resolver.resolveException(request, response, handlerMethod, e);
        System.out.println(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    static class Controller5 {
        public void foo() {

        }
    }
}
