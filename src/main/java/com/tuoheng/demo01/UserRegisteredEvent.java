package com.tuoheng.demo01;

import org.springframework.context.ApplicationEvent;

/**
 * @Description 事件类
 * @Author douzhenjun
 * @DATE 2023/4/19
 **/
public class UserRegisteredEvent extends ApplicationEvent {
    public UserRegisteredEvent(Object source){
        super(source);
    }
}
