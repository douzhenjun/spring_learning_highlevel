package com.tuoheng.demo01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/19
 **/
@Component
public class ComponentB {
    
    private static final Logger log = LoggerFactory.getLogger(ComponentB.class);
    
    @EventListener
    public void handle(UserRegisteredEvent event){
        log.debug("{}", event);
        log.debug("发送短信");
    }
}
