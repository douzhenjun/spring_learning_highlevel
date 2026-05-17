package com.tuoheng.demo01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/19
 **/
@Component
public class ComponentA {
    
    private static final Logger log = LoggerFactory.getLogger(ComponentA.class);
    
    @Autowired
    private ApplicationEventPublisher context;
    
    public void register(){
        log.debug("用户注册");
        context.publishEvent(new UserRegisteredEvent(this));
    }
}
