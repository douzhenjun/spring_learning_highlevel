package com.tuoheng.demo13;

import org.springframework.cglib.core.Signature;

/**
 * 模拟cglib代理的实现,MethodProxy.invoke(target, params)
 */
public class Demo3 {

    public static void main(String[] args) {
        Target target = new Target();
        TargetFastClass targetFastClass = new TargetFastClass();
        int index = targetFastClass.getIndex(new Signature("save", "()V"));
        targetFastClass.invoke(index, target, new Object[0]);
        index = targetFastClass.getIndex(new Signature("save", "(I)V"));
        targetFastClass.invoke(index, target, new Object[]{100});
        index = targetFastClass.getIndex(new Signature("save", "(J)V"));
        targetFastClass.invoke(index, target, new Object[]{1000L});
    }
}
