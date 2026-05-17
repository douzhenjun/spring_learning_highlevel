package com.tuoheng.demo08;

import com.tuoheng.demo08.sub.E;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 向单例对象中注入scope=prototype的多例对象,多例对象并未产生效果,有这么四个解决办法
 * --add-opens java.base/java.lang=ALL-UNNAMED
 */
@ComponentScan("com.tuoheng.demo08.sub")
public class Demo08_02 {

    private static final Logger log = LoggerFactory.getLogger(Demo08_02.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Demo08_02.class);

        E e = context.getBean(E.class);
        log.debug("{}", e.getF1().getClass());
        log.debug("{}", e.getF1());
        log.debug("{}", e.getF1());
        log.debug("{}", e.getF1());

        log.debug("{}", e.getF2().getClass());
        log.debug("{}", e.getF2());
        log.debug("{}", e.getF2());
        log.debug("{}", e.getF2());

        log.debug("{}", e.getF3());
        log.debug("{}", e.getF3());

        log.debug("{}", e.getF4());
        log.debug("{}", e.getF4());

        context.close();
    }
}
