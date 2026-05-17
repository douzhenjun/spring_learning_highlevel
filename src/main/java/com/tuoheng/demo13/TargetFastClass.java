package com.tuoheng.demo13;

import org.springframework.cglib.core.Signature;

/**
 * 当执行MethodProxy.invoke(target, params)时, 这里target表示被代理对象, 它实际上
 * 调用了TargetFastClass(也是一个代理对象)的getIndex方法和invoke方法, 避免了反射调用方法
 */
public class TargetFastClass {
    
    static Signature s0 = new Signature("save", "()V");
    static Signature s1 = new Signature("save", "(I)V");
    static Signature s2 = new Signature("save", "(J)V");
    
    /**
     * 获取代理方法的编号
     */
    public int getIndex(Signature signature){
        if(s0.equals(signature)){
            return 0;
        }else if(s1.equals(signature)){
            return 1;
        }else if(s2.equals(signature)){
            return 2;
        }
        return -1;
    }
    
    /**
     * 根据index数据判断执行何种方法
     */
    public Object invoke(int index, Object target, Object[] args){
        if(index == 0){
            ((Target) target).save();
            return null;
        }else if(index == 1){
            ((Target) target).save((int)args[0]);
            return null;
        }else if(index == 2){
            ((Target) target).save((long)args[0]);
            return null;
        }
        throw new RuntimeException("无此方法");
    }
}
