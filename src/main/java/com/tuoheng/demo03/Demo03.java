package com.tuoheng.demo03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/20
 **/
@SpringBootApplication
public class Demo03 {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Demo03.class);
        context.close();
    }
}
