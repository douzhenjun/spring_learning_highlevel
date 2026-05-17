package com.tuoheng.demo07;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @Description 初始化和销毁的执行顺序
 * @Author douzhenjun
 * @DATE 2023/4/24
 **/
@SpringBootApplication
public class Demo1 {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Demo1.class);
        context.close();
    }
    
    @Bean(initMethod = "init3")
    public Bean1 bean1(){
        return new Bean1();
    }
    
    @Bean(destroyMethod = "destroy3")
    public Bean2 bean2(){
        return new Bean2();
    }
}
